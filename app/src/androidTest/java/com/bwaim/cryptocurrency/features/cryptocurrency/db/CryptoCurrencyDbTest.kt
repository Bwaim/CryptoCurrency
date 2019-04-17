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

import android.content.Context
import androidx.room.Room
import androidx.room.paging.LimitOffsetDataSource
import androidx.test.core.app.ApplicationProvider
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrency
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CryptoCurrencyDbTest {

    private lateinit var cryptoCurrencyDao: CryptoCurrencyDao
    private lateinit var db: CryptoCurrencyDb

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, CryptoCurrencyDb::class.java
        ).build()
        cryptoCurrencyDao = db.cryptoCurrencies()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun writeCryptoCurrencyAndRead() {
        val cryptoCurrencies = arrayListOf(
            CryptoCurrency(1, "BTC", "Bitcoin", "url", "B", 450.0, 0),
            CryptoCurrency(2, "ETH", "Etherium", "url", "E", 45.0, 1)
        )

        cryptoCurrencyDao.insert(cryptoCurrencies)

        val result = cryptoCurrencyDao.getAll()
        assertEquals(cryptoCurrencies.size, (result.create() as LimitOffsetDataSource).countItems())
        assertEquals(cryptoCurrencies, (result.create() as LimitOffsetDataSource).loadRange(0, cryptoCurrencies.size))
    }
}