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

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrency
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrencyDetail
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrencyJoin
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.DataInfo

@Dao
interface CryptoCurrencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cryptoCurrencies: List<CryptoCurrency>)

    @Query("SELECT * FROM cryptoCurrencies ORDER BY indexInResponse")
    fun getAll(): DataSource.Factory<Int, CryptoCurrency>

    @Query("SELECT MAX(indexInResponse) + 1 FROM cryptoCurrencies")
    fun getNextIndex(): Int

    @Query("DELETE FROM cryptoCurrencies")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDataTime(updateTime: DataInfo)

    @Query("SELECT * FROM dataInfo")
    fun getDataTime(): DataInfo?

    @Query("SELECT count('x') FROM cryptoCurrencies")
    fun getNbCurrencies(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDetails(cryptoCurrencyDetail: CryptoCurrencyDetail)

    @Query("SELECT * FROM cryptoCurrencies cc INNER JOIN cryptoCurrencyDetails ccd ON cc.name == ccd.nameSymbol WHERE cc.name = :symbol")
    fun selectDetails(symbol: String): LiveData<CryptoCurrencyJoin>

    @Query("DELETE FROM cryptoCurrencyDetails")
    fun deleteAllDetails()
}