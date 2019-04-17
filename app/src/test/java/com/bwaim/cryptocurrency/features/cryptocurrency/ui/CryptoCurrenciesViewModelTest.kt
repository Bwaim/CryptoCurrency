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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.bwaim.cryptocurrency.core.executor.PostExecutionThread
import com.bwaim.cryptocurrency.features.cryptocurrency.repository.CryptoCurrencyRepository
import com.bwaim.cryptocurrency.features.cryptocurrency.repository.Listing
import com.bwaim.cryptocurrency.features.cryptocurrency.repository.NetworkState
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrency
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit

@DisplayName("Test of the CryptoCurrencies ViewModel")
@RunWith(JUnit4::class)
class CryptoCurrenciesViewModelTest {

    companion object {
        private const val PAGE_SIZE = 20
        private const val REFRESH_INTERVAL = 2L
        private const val NB_INTERVAL = 3
    }

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private var cryptoCurrenciesViewModel: CryptoCurrenciesViewModel? = null

    private lateinit var testScheduler: TestScheduler

    @Mock
    private lateinit var mockCryptoCurrencyRepository: CryptoCurrencyRepository
    @Mock
    private lateinit var mockPostExecutionThread: PostExecutionThread
    @Mock
    private lateinit var mockPagedList: LiveData<PagedList<CryptoCurrency>>
    @Mock
    private lateinit var mockNetworkState: LiveData<NetworkState>
    @Mock
    private lateinit var mockRefreshState: LiveData<NetworkState>
    @Mock
    private lateinit var mockRefresh: (withFeedback: Boolean) -> Unit
    @Mock
    private lateinit var mockRetry: () -> Unit

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        testScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
    }

    @Test
    fun testInstallAutoRefresh() {
        val listing = Listing(mockPagedList, mockNetworkState, mockRefreshState, mockRefresh, mockRetry)
        given(mockCryptoCurrencyRepository.getCryptoCurrencies(PAGE_SIZE)).willReturn(listing)
        given(mockPostExecutionThread.getScheduler()).willReturn(testScheduler)

        cryptoCurrenciesViewModel = CryptoCurrenciesViewModel(mockCryptoCurrencyRepository, mockPostExecutionThread)
        testScheduler.advanceTimeBy(REFRESH_INTERVAL * NB_INTERVAL, TimeUnit.MINUTES)

        verify(mockRefresh, times(NB_INTERVAL)).invoke(false)
    }
}