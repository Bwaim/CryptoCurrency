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

package com.bwaim.cryptocurrency.core.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.bwaim.cryptocurrency.core.extension.networkInfo
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injectable class which returns information about the network connection state.
 */
@Singleton
class NetworkHandler
@Inject constructor(private val context: Context) {
    private var networkCapabilities: NetworkCapabilities? = null

    init {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Bug in android 6.0 needing WRITE_SETTINGS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && "6.0" != Build.VERSION.RELEASE) {
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()
            try {
                connectivityManager.requestNetwork(networkRequest, object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)
                        networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                    }
                })
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    // TODO - network capabilities can be null
    @SuppressWarnings("deprecation")
    fun isConnected(): Boolean {
        val networkInfo = context.networkInfo
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && "6.0" != Build.VERSION.RELEASE) {
            !(networkInfo == null
                    || !networkInfo.isConnected
                    || networkCapabilities == null
                    || (!(networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                ?: true)
                    && !(networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                ?: true)))
        } else {
            // networkInfo.getType is deprecated in API 28, so not for this branch
            !(networkInfo == null
                    || !networkInfo.isConnected
                    || (networkInfo.type != ConnectivityManager.TYPE_WIFI
                    && networkInfo.type != ConnectivityManager.TYPE_MOBILE))
        }
    }
}