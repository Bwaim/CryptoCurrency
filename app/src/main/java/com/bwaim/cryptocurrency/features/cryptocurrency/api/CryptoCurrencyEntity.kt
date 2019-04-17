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

import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrency
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrencyDetail
import com.google.gson.annotations.SerializedName

/**
 * Classes to represent the json responses.
 */
data class CryptoCurrencyResponse(val entities: List<CryptoCurrencyEntity>)

data class CryptoCurrencyEntity(
    @SerializedName("Id")
    val id: Int,
    @SerializedName("Name")
    val name: String,
    @SerializedName("FullName")
    val fullName: String,
    @SerializedName("ImageUrl")
    val imageUrl: String,
    var symbol: String,
    var price: Double
) {
    fun toCryptoCurrency(index: Int) = CryptoCurrency(id, name, fullName, imageUrl, symbol, price, index)
}

data class CryptoCurrencyDetailsResponse(val volumes: Array<CryptoCurrencyDetailsEntity>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CryptoCurrencyDetailsResponse

        if (!volumes.contentEquals(other.volumes)) return false

        return true
    }

    override fun hashCode(): Int {
        return volumes.contentHashCode()
    }

    fun toCryptoCurrencyDetail(symbol: String): CryptoCurrencyDetail? {
        if (volumes.size != 2) {
            return null
        }
        return CryptoCurrencyDetail(
            symbol,
            last24Volume = volumes[0].volumeFrom,
            last24VolumeEUR = volumes[0].volumeTo,
            currentVolume = volumes[1].volumeFrom,
            currentVolumeEur = volumes[1].volumeTo
        )
    }
}

data class CryptoCurrencyDetailsEntity(
    @SerializedName("volumefrom")
    val volumeFrom: Double,
    @SerializedName("volumeto")
    val volumeTo: Double
)