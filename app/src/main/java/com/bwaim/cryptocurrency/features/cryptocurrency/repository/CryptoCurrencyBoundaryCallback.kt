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
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.bwaim.cryptocurrency.core.executor.ThreadExecutor
import com.bwaim.cryptocurrency.core.platform.NetworkHandler
import com.bwaim.cryptocurrency.features.cryptocurrency.api.CryptoCompareApi
import com.bwaim.cryptocurrency.features.cryptocurrency.api.CryptoCurrencyResponse
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrency
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * Callback to manage the boundaries of the pagedList object. *
 */
class CryptoCurrencyBoundaryCallback(
    private val webservice: CryptoCompareApi,
    private val handleResponse: (CryptoCurrencyResponse, Int, Int) -> Unit,
    private val networkPageSize: Int,
    private val threadExecutor: ThreadExecutor,
    private val networkHandler: NetworkHandler
) : PagedList.BoundaryCallback<CryptoCurrency>() {

    val networkState = MutableLiveData<NetworkState>()
    private val disposables = CompositeDisposable()

    @MainThread
    override fun onZeroItemsLoaded() {
        if (networkHandler.isConnected()) {
            networkState.postValue(NetworkState.LOADING)
            requestCryptoCurrencies(0)
        } else {
            networkState.postValue(NetworkState.error("Not connected !"))
        }
    }

    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: CryptoCurrency) {
        if (networkHandler.isConnected()) {
            networkState.postValue(NetworkState.LOADING)
            val numPage = itemAtEnd.indexInResponse / networkPageSize + 1
            requestCryptoCurrencies(numPage)
        }
    }

    private fun insertItemsIntoDb(response: CryptoCurrencyResponse, numPage: Int) {
        handleResponse(response, numPage, networkPageSize)
        networkState.postValue(NetworkState.LOADED)
    }

    fun retry() = requestCryptoCurrencies(0)

    private fun requestCryptoCurrencies(numPage: Int) {
        val observable = webservice.cryptoCurrencies(
            limit = networkPageSize,
            page = numPage
        ).subscribeOn(Schedulers.from(threadExecutor))

        disposables.add(observable.subscribeBy(
            onError = {
                Timber.d(it, "Network error")
                networkState.postValue(NetworkState.error(it.message))
            },
            onSuccess = { insertItemsIntoDb(it, numPage) }
        ))
    }

    fun dispose() {
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
    }
}