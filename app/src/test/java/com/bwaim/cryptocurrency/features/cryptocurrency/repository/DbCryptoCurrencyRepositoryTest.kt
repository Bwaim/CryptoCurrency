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

import com.bwaim.cryptocurrency.core.executor.JobExecutor
import com.bwaim.cryptocurrency.core.platform.NetworkHandler
import com.bwaim.cryptocurrency.features.cryptocurrency.api.CryptoCompareApi
import com.bwaim.cryptocurrency.features.cryptocurrency.api.CryptoCurrencyDetailsResponse
import com.bwaim.cryptocurrency.features.cryptocurrency.db.CryptoCurrencyDao
import com.bwaim.cryptocurrency.features.cryptocurrency.db.CryptoCurrencyDb
import com.bwaim.cryptocurrency.features.cryptocurrency.repository.CryptoCurrencyRepository.DbCryptoCurrencyRepository
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrencyDetail
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.*
import org.mockito.BDDMockito.given
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times

@DisplayName("Test of the CryptoCurrencyBoundaryCallback")
@RunWith(JUnit4::class)
internal class DbCryptoCurrencyRepositoryTest {

    companion object {
        private const val SYMBOL = "BTC"
    }

    private lateinit var cryptoCurrencyRepository: DbCryptoCurrencyRepository

    @Mock
    private lateinit var mockDb: CryptoCurrencyDb
    @Mock
    private lateinit var mockCryptoApi: CryptoCompareApi
    @Mock
    private lateinit var mockNetworkHandler: NetworkHandler
    @Mock
    private lateinit var mockCryptoCurrencyDetailsResponse: CryptoCurrencyDetailsResponse
    @Mock
    private lateinit var mockCryptoCurrencyDao: CryptoCurrencyDao


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(mockDb.runInTransaction(ArgumentMatchers.any())).thenCallRealMethod()
        cryptoCurrencyRepository = DbCryptoCurrencyRepository(mockDb, mockCryptoApi, JobExecutor(), mockNetworkHandler)
    }

    @Test
    @DisplayName("Test updateCryptoCurrencyDetail - No Network")
    fun testUpdateCryptoCurrencyDetailNoNetwork() {
        BDDMockito.given(mockNetworkHandler.isConnected()).willReturn(false)

        cryptoCurrencyRepository.updateCryptoCurrencyDetail(SYMBOL)

        Mockito.verifyZeroInteractions(mockCryptoApi)
        Mockito.verifyZeroInteractions(mockDb)
    }

    @Test
    @DisplayName("Test updateCryptoCurrencyDetail - Happy case")
    fun testUpdateCryptoCurrencyDetailHappyCase() {
        val cryptoCurrencyDetail = CryptoCurrencyDetail("BTC", 14.0, 25.0, 152.0, 230.0)

        given(mockNetworkHandler.isConnected()).willReturn(true)
        given(mockCryptoApi.getCryptoCurrencyDetails(SYMBOL)).willReturn(Single.just(mockCryptoCurrencyDetailsResponse))
        given(mockCryptoCurrencyDetailsResponse.toCryptoCurrencyDetail(SYMBOL)).willReturn(cryptoCurrencyDetail)
        given(mockDb.cryptoCurrencies()).willReturn(mockCryptoCurrencyDao)

        cryptoCurrencyRepository.updateCryptoCurrencyDetail(SYMBOL)

        Mockito.verify(mockCryptoCurrencyDao, times(1)).insertDetails(cryptoCurrencyDetail)
    }
}