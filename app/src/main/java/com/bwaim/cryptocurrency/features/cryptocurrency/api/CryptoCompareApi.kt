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

package com.bwaim.cryptocurrency.features.cryptocurrency.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API communication with https://min-api.cryptocompare.com/
 *
 */
interface CryptoCompareApi {

    companion object {
        const val BASE_URL = "https://min-api.cryptocompare.com/"
        private const val GET_ALL_CRYPTO_CURRENCIES = "/data/top/mktcapfull"
        private const val GET_CRYPTO_CURRENCY_VOLUME = "/data/histoday"
    }

    @GET(GET_ALL_CRYPTO_CURRENCIES)
    fun cryptoCurrencies(
        @Query("limit") limit: Int,
        @Query("page") page: Int = 0,
        @Query("tsym") toSymbol: String = "EUR"
    ): Single<CryptoCurrencyResponse>

    @GET(GET_CRYPTO_CURRENCY_VOLUME)
    fun getCryptoCurrencyDetails(
        @Query("fsym") fromSymbol: String,
        @Query("tsym") toSymbol: String = "EUR",
        @Query("limit") limit: Int = 1
    ): Single<CryptoCurrencyDetailsResponse>
}