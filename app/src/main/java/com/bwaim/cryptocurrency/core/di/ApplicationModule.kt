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

package com.bwaim.cryptocurrency.core.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.bwaim.cryptocurrency.BuildConfig
import com.bwaim.cryptocurrency.core.di.viewmodel.ViewModelModule
import com.bwaim.cryptocurrency.core.executor.JobExecutor
import com.bwaim.cryptocurrency.core.executor.PostExecutionThread
import com.bwaim.cryptocurrency.core.executor.ThreadExecutor
import com.bwaim.cryptocurrency.core.executor.UiThread
import com.bwaim.cryptocurrency.features.cryptocurrency.api.*
import com.bwaim.cryptocurrency.features.cryptocurrency.db.CryptoCurrencyDb
import com.bwaim.cryptocurrency.features.cryptocurrency.repository.CryptoCurrencyRepository
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class ApplicationModule {

    @Provides
    @Singleton
    fun provideApplicationContext(application: Application): Context = application

    @Provides
    @Singleton
    fun provideCryptoCompareService(): CryptoCompareApi {
        return Retrofit.Builder()
            .baseUrl(HttpUrl.parse(CryptoCompareApi.BASE_URL)!!)
            .client(createClient())
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .registerTypeAdapter(
                            CryptoCurrencyResponse::class.java,
                            CryptoCurrencyDeserializer()
                        )
                        .registerTypeAdapter(
                            CryptoCurrencyDetailsResponse::class.java,
                            CryptoCurrencyDetailsDeserializer()
                        )
                        .create()
                )
            )
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(CryptoCompareApi::class.java)
    }

    private fun createClient(): OkHttpClient {
        val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
            okHttpClientBuilder.addInterceptor(loggingInterceptor)
        }
        return okHttpClientBuilder.build()
    }

    @Provides
    @Singleton
    fun provideDb(app: Application): CryptoCurrencyDb {
        return Room.databaseBuilder(app, CryptoCurrencyDb::class.java, "cryptoCurrency.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCryptoCurrencyRepository(
        dbCryptoCurrencyRepository: CryptoCurrencyRepository.DbCryptoCurrencyRepository
    ): CryptoCurrencyRepository {
        return dbCryptoCurrencyRepository
    }

    @Provides
    @Singleton
    fun provideThreadExecutor(jobExecutor: JobExecutor): ThreadExecutor = jobExecutor

    @Provides
    @Singleton
    fun providePostExecutionThread(uiThread: UiThread): PostExecutionThread = uiThread
}