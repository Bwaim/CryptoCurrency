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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bwaim.cryptocurrency.core.executor.JobExecutor
import com.bwaim.cryptocurrency.core.platform.NetworkHandler
import com.bwaim.cryptocurrency.features.cryptocurrency.api.CryptoCompareApi
import com.bwaim.cryptocurrency.features.cryptocurrency.api.CryptoCurrencyResponse
import com.bwaim.cryptocurrency.rules.RxImmediateSchedulerRule
import io.reactivex.Single
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations

@DisplayName("Test of the CryptoCurrencyBoundaryCallback")
@RunWith(JUnit4::class)
internal class CryptoCurrencyBoundaryCallbackTest {

    companion object {
        private const val DEFAULT_NETWORK_PAGE_SIZE = 10

        // Test rule for making the RxJava to run synchronously in unit test
        @ClassRule
        @JvmField
        val schedulers = RxImmediateSchedulerRule()
    }

    private lateinit var cryptoCurrencyBoundaryCallback: CryptoCurrencyBoundaryCallback

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockWebservice: CryptoCompareApi
    @Mock
    private lateinit var mockHandleResponse: (CryptoCurrencyResponse, Int, Int) -> Unit
    @Mock
    private lateinit var mockNetworkHandler: NetworkHandler
    @Mock
    private lateinit var mockCryptoCurrencyResponse: CryptoCurrencyResponse

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        cryptoCurrencyBoundaryCallback = CryptoCurrencyBoundaryCallback(
            mockWebservice,
            mockHandleResponse,
            DEFAULT_NETWORK_PAGE_SIZE,
            JobExecutor(),
            mockNetworkHandler
        )
    }

    @Test
    @DisplayName("Test onZeroItemsLoaded - No network")
    fun testOnZeroItemsLoadedNotNetwork() {
        given(mockNetworkHandler.isConnected()).willReturn(false)

        cryptoCurrencyBoundaryCallback.onZeroItemsLoaded()

        Mockito.verifyZeroInteractions(mockHandleResponse)
    }

    @Test
    @DisplayName("Test onZeroItemsLoaded - Happy case")
    fun testOnZeroItemsLoadedHappyCase() {
        given(mockNetworkHandler.isConnected()).willReturn(true)
        given(mockWebservice.cryptoCurrencies(DEFAULT_NETWORK_PAGE_SIZE)).willReturn(
            Single.just(
                mockCryptoCurrencyResponse
            )
        )

        cryptoCurrencyBoundaryCallback.onZeroItemsLoaded()

        Mockito.verify(mockHandleResponse, times(1)).invoke(mockCryptoCurrencyResponse, 0, DEFAULT_NETWORK_PAGE_SIZE)
    }
}