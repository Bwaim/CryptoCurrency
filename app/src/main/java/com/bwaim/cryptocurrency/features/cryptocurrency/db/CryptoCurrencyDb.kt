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

package com.bwaim.cryptocurrency.features.cryptocurrency.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bwaim.cryptocurrency.core.db.DataConverters
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrency
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrencyDetail
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.DataInfo

/**
 * Database schema used by the DbCryptoCurrencyRepository.
 */
@Database(
    entities = [CryptoCurrency::class, DataInfo::class, CryptoCurrencyDetail::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DataConverters::class)
abstract class CryptoCurrencyDb : RoomDatabase() {
    abstract fun cryptoCurrencies(): CryptoCurrencyDao
}
