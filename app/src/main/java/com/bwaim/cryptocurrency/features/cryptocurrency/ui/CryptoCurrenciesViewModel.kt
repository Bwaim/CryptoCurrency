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

package com.bwaim.cryptocurrency.features.cryptocurrency.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bwaim.cryptocurrency.core.executor.PostExecutionThread
import com.bwaim.cryptocurrency.core.platform.BaseViewModel
import com.bwaim.cryptocurrency.features.cryptocurrency.repository.CryptoCurrencyRepository
import com.bwaim.cryptocurrency.features.cryptocurrency.repository.Listing
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrency
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CryptoCurrenciesViewModel
@Inject constructor(
    private val cryptoCurrencyRepository: CryptoCurrencyRepository,
    private val postExecutionThread: PostExecutionThread
) : BaseViewModel() {

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private const val REFRESH_INTERVAL = 2L // in minutes
    }

    private val repoResult = MutableLiveData<Listing<CryptoCurrency>>()
    val cryptoCurrencies = Transformations.switchMap(repoResult) { it.pagedList }!!
    val networkState = Transformations.switchMap(repoResult) { it.networkState }!!
    val refreshState = Transformations.switchMap(repoResult) { it.refreshState }!!


    init {
        repoResult.value = cryptoCurrencyRepository.getCryptoCurrencies(DEFAULT_PAGE_SIZE)
        installAutoRefresh()
    }

    override fun onCleared() {
        cryptoCurrencyRepository.dispose()
        super.onCleared()
    }

    private fun installAutoRefresh() {
        disposables.add(Observable.interval(REFRESH_INTERVAL, TimeUnit.MINUTES)
            .observeOn(postExecutionThread.getScheduler())
            .subscribeBy(
                onError = { Timber.d(it, "Network error") },
                onNext = { refresh(false) }
            ))
    }

    fun refresh(withFeedback: Boolean = true) {
        repoResult.value?.refresh?.invoke(withFeedback)
    }

    fun retry() {
        val listing = repoResult.value
        listing?.retry?.invoke()
    }
}