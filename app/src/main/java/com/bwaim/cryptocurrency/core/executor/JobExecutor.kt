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

package com.bwaim.cryptocurrency.core.executor

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobExecutor @Inject constructor() : ThreadExecutor {

    private val threadPoolExecutor = ThreadPoolExecutor(
        3, 5, 10, TimeUnit.SECONDS,
        LinkedBlockingQueue(), JobThreadFactory()
    )

    override fun execute(command: Runnable?) = threadPoolExecutor.execute(command)

    private class JobThreadFactory(var counter: Int = 0) : ThreadFactory {
        override fun newThread(runnable: Runnable) = Thread(runnable, "android_" + counter++)
    }
}