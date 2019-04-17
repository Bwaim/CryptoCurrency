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

package com.bwaim.cryptocurrency.features.cryptocurrency.vo

import androidx.room.*

@Entity(
    tableName = "cryptoCurrencies",
    indices = [Index(value = ["name"], unique = true)]
)
data class CryptoCurrency(
    @PrimaryKey
    val id: Int,
    val name: String,
    val fullName: String,
    val imageUrl: String,
    val symbol: String,
    val price: Double,
    val indexInResponse: Int
)

@Entity(
    tableName = "cryptoCurrencyDetails",
    indices = [Index(value = ["nameSymbol"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = CryptoCurrency::class,
        parentColumns = arrayOf("name"),
        childColumns = arrayOf("nameSymbol")
    )]
)
data class CryptoCurrencyDetail(
    @PrimaryKey
    val nameSymbol: String,
    val currentVolume: Double,
    val last24Volume: Double,
    val currentVolumeEur: Double,
    val last24VolumeEUR: Double
)

data class CryptoCurrencyJoin(
    @Embedded
    val cryptoCurrency: CryptoCurrency,
    @Embedded
    val cryptoCurrencyDetail: CryptoCurrencyDetail
)