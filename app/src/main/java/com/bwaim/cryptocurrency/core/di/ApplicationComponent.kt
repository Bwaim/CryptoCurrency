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
import com.bwaim.cryptocurrency.CryptoCurrencyApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

/**
 * Created by Fabien Boismoreau on 13/04/2019.
 * <p>
 */
@Singleton
@Component(
    modules = [AndroidInjectionModule::class,
        ApplicationModule::class,
        ActivityBindingModule::class]
)
interface ApplicationComponent : AndroidInjector<CryptoCurrencyApplication> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): ApplicationComponent
    }

    override fun inject(cryptoCurrencyApplication: CryptoCurrencyApplication)
}