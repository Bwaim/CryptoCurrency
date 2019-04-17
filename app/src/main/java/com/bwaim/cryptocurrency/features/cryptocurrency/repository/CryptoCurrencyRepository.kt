/*
 *    Copyright 2019 Fabien Boismoreau
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.bwaim.cryptocurrency.features.cryptocurrency.repository

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import com.bwaim.cryptocurrency.core.executor.ThreadExecutor
import com.bwaim.cryptocurrency.core.platform.NetworkHandler
import com.bwaim.cryptocurrency.features.cryptocurrency.api.CryptoCompareApi
import com.bwaim.cryptocurrency.features.cryptocurrency.api.CryptoCurrencyDetailsResponse
import com.bwaim.cryptocurrency.features.cryptocurrency.api.CryptoCurrencyResponse
import com.bwaim.cryptocurrency.features.cryptocurrency.db.CryptoCurrencyDb
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrency
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrencyJoin
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.DataInfo
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.min

/**
 * Repository to access the data.
 * Data are fetched from the web Api and inserted in the room DB.
 * The Ui observe the DB.
 */
interface CryptoCurrencyRepository {

    fun getCryptoCurrencies(pageSize: Int): Listing<CryptoCurrency>
    fun updateCryptoCurrencyDetail(symbol: String)
    fun getDetail(symbol: String): LiveData<CryptoCurrencyJoin>
    fun dispose()

    class DbCryptoCurrencyRepository
    @Inject constructor(
        private val db: CryptoCurrencyDb,
        private val cryptoApi: CryptoCompareApi,
        private val threadExecutor: ThreadExecutor,
        private val networkHandler: NetworkHandler
    ) : CryptoCurrencyRepository {
        companion object {
            private const val DEFAULT_NETWORK_PAGE_SIZE = 10
            private const val MAX_LIMIT = 100
            private const val CACHE_VALIDITY = 20L // In minutes
        }

        private val networkPageSize: Int = DEFAULT_NETWORK_PAGE_SIZE
        private val disposables = CompositeDisposable()

        /**
         * Insert items fetched from web API inside the room DB.
         * The index of the item is computed page number and the page size of the web request.
         * The date od the update is inserted in the DB for the cache validity.
         */
        private fun insertCryptoCurrenciesIntoDb(response: CryptoCurrencyResponse, numPage: Int, pageSize: Int) {
            val cryptoCurrencies = response.entities
            val currentTime = Date()
            cryptoCurrencies.let { cryptoCurrenciesEntity ->
                db.runInTransaction {
                    val start = numPage * pageSize
                    val items = cryptoCurrenciesEntity.mapIndexed { index, child ->
                        child.toCryptoCurrency(start + index)
                    }
                    db.cryptoCurrencies().insert(items)
                    db.cryptoCurrencies().insertDataTime(DataInfo(1, currentTime))
                }
            }
        }

        /**
         * Check the cache validity and delete data if the cache is no longer valid.
         */
        private fun checkCacheValidity() {
            val job = GlobalScope.async(Dispatchers.IO) {
                val currentDate = Date()
                db.runInTransaction {
                    val lastLoadDate = db.cryptoCurrencies().getDataTime()
                    val diff = currentDate.time - (lastLoadDate?.lastUpdateTime?.time
                        ?: currentDate.time)
                    if (TimeUnit.MILLISECONDS.convert(CACHE_VALIDITY, TimeUnit.MINUTES) < diff) {
                        db.cryptoCurrencies().deleteAllDetails()
                        db.cryptoCurrencies().deleteAll()
                    }
                }
            }
            GlobalScope.launch(Dispatchers.Main) { job.await() }
        }

        /**
         * Refresh all the data of the Db.
         * The page size is optimised.
         */
        @MainThread
        private fun refresh(withFeedback: Boolean): LiveData<NetworkState> {
            val networkState = MutableLiveData<NetworkState>()
            if (networkHandler.isConnected()) {
                if (withFeedback) {
                    networkState.value = NetworkState.LOADING
                }
                val job = GlobalScope.async(Dispatchers.IO) {
                    var nbItems = 0
                    var numPages = 0
                    db.runInTransaction {
                        nbItems = db.cryptoCurrencies().getNbCurrencies()
                    }

                    val requests = ArrayList<Pair<Int, Int>>()
                    while (nbItems > 0) {
                        val itemsToGet = min(nbItems, MAX_LIMIT)
                        requests.add(Pair(itemsToGet, numPages))
                        nbItems -= MAX_LIMIT
                        numPages++
                    }

                    val requestObservable = Observable.fromIterable<Pair<Int, Int>>(requests)
                    disposables.add(requestObservable.flatMap { params ->
                        val observable = cryptoApi.cryptoCurrencies(
                            limit = params.first,
                            page = params.second
                        ).subscribeOn(Schedulers.from(threadExecutor))

                        disposables.add(observable.subscribeBy(
                            onError = { networkState.postValue(NetworkState.error(it.message)) },
                            onSuccess = {
                                insertCryptoCurrenciesIntoDb(it, params.second, MAX_LIMIT)
                                networkState.postValue(NetworkState.LOADED)
                            }
                        ))

                        observable.toObservable()
                    }.subscribeBy(
                        onComplete = { networkState.postValue(NetworkState.LOADED) }
                    ))
                }
                GlobalScope.launch(Dispatchers.Main) { job.await() }
            } else {
                networkState.value = NetworkState.LOADED
            }
            return networkState
        }

        /**
         * Returns a Listing used to refresh the ui.
         */
        @MainThread
        override fun getCryptoCurrencies(pageSize: Int): Listing<CryptoCurrency> {

            checkCacheValidity()
            // create a boundary callback which will observe when the user reaches to the edges of
            // the list and update the database with extra data.
            val boundaryCallback = CryptoCurrencyBoundaryCallback(
                webservice = cryptoApi,
                handleResponse = this::insertCryptoCurrenciesIntoDb,
                networkPageSize = networkPageSize,
                threadExecutor = threadExecutor,
                networkHandler = networkHandler
            )
            // we are using a mutable live data to trigger refresh requests which eventually calls
            // refresh method and gets a new live data. Each refresh request by the user becomes a newly
            // dispatched data in refreshTrigger
            val refreshTrigger = MutableLiveData<Boolean>()
            val refreshState = Transformations.switchMap(refreshTrigger) {
                refresh(it)
            }

            val livePagedList = db.cryptoCurrencies().getAll().toLiveData(
                pageSize = DEFAULT_NETWORK_PAGE_SIZE,
                boundaryCallback = boundaryCallback
            )

            return Listing(
                pagedList = livePagedList,
                networkState = boundaryCallback.networkState,
                retry = { boundaryCallback.retry() },
                refresh = { refreshTrigger.value = it },
                refreshState = refreshState
            )
        }

        /**
         * Get fresh data from the api for the detail of the crypto currency.
         */
        override fun updateCryptoCurrencyDetail(symbol: String) {
            if (networkHandler.isConnected()) {
                val observable = cryptoApi.getCryptoCurrencyDetails(
                    fromSymbol = symbol
                ).subscribeOn(Schedulers.from(threadExecutor))

                disposables.add(observable.subscribeBy(
                    onError = { Timber.d(it, "Network error") },
                    onSuccess = { insertCryptoCurrencyDetailsIntoDb(it, symbol) }
                ))
            }
        }

        /**
         * Insert the detail of the currency in the room DB.
         */
        private fun insertCryptoCurrencyDetailsIntoDb(response: CryptoCurrencyDetailsResponse, symbol: String) {
            val cryptoCurrencyDetail = response.toCryptoCurrencyDetail(symbol)
            cryptoCurrencyDetail?.let {
                db.runInTransaction {
                    db.cryptoCurrencies().insertDetails(it)
                }
            }
        }

        /**
         * Request the detail of the crypto currency from the room db.
         */
        override fun getDetail(symbol: String): LiveData<CryptoCurrencyJoin> {
            return db.cryptoCurrencies().selectDetails(symbol)
        }

        override fun dispose() {
            if (!disposables.isDisposed) {
                disposables.dispose()
            }
        }
    }
}