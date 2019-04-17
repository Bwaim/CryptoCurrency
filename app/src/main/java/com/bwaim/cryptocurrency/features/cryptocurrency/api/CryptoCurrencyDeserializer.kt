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

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Json Deserializer to facilitate the reading of the web API.
 */
class CryptoCurrencyDeserializer : JsonDeserializer<CryptoCurrencyResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): CryptoCurrencyResponse {
        val data = json?.asJsonObject?.get("Data")
        val cryptoCurrencies = ArrayList<CryptoCurrencyEntity>()
        for (i in 0 until (data?.asJsonArray?.size() ?: 0)) {
            val currency = data?.asJsonArray?.get(i)
            val coinInfo = currency?.asJsonObject?.get("CoinInfo")
            val entity = Gson().fromJson(coinInfo, CryptoCurrencyEntity::class.java)
            val currencyKey = currency?.asJsonObject?.get("DISPLAY")?.asJsonObject?.keySet()?.elementAt(0)
            entity.symbol =
                currency?.asJsonObject?.get("DISPLAY")?.asJsonObject?.get(currencyKey)?.asJsonObject?.get("FROMSYMBOL")?.asString
                    ?: ""
            entity.price =
                currency?.asJsonObject?.get("RAW")?.asJsonObject?.get(currencyKey)?.asJsonObject?.get("PRICE")?.asDouble
                    ?: 0.0
            cryptoCurrencies.add(entity)
        }

        return CryptoCurrencyResponse(cryptoCurrencies)
    }
}

class CryptoCurrencyDetailsDeserializer : JsonDeserializer<CryptoCurrencyDetailsResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): CryptoCurrencyDetailsResponse {
        val data = json?.asJsonObject?.get("Data")
        val volumes: MutableList<CryptoCurrencyDetailsEntity> = arrayListOf()
        for (i in 0 until (data?.asJsonArray?.size() ?: 0)) {
            val infos = data?.asJsonArray?.get(i)
            volumes.add(i, Gson().fromJson(infos, CryptoCurrencyDetailsEntity::class.java))
        }
        return CryptoCurrencyDetailsResponse(volumes.toTypedArray())
    }
}