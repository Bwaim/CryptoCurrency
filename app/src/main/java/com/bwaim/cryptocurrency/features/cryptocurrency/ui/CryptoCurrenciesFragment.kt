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

package com.bwaim.cryptocurrency.features.cryptocurrency.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.PagedList
import com.bwaim.cryptocurrency.R
import com.bwaim.cryptocurrency.core.di.Injectable
import com.bwaim.cryptocurrency.core.extension.viewModel
import com.bwaim.cryptocurrency.core.platform.GlideApp
import com.bwaim.cryptocurrency.features.cryptocurrency.repository.NetworkState
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrency
import kotlinx.android.synthetic.main.fragment_crypto_currencies.*
import javax.inject.Inject

class CryptoCurrenciesFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var cryptoCurrenciesViewModel: CryptoCurrenciesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_crypto_currencies, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cryptoCurrenciesViewModel = viewModel(viewModelFactory)
        initAdapter()
        initSwipeToRefresh()
        cryptoCurrenciesViewModel.loadTrigger.value = null
    }

    private fun initAdapter() {
        val glide = GlideApp.with(this)
        val adapter = CryptoCurrencyAdapter(glide, ::navigate) {
            cryptoCurrenciesViewModel.retry()
        }
        rv_crypto_currencies.adapter = adapter
        cryptoCurrenciesViewModel.cryptoCurrencies.observe(viewLifecycleOwner, Observer<PagedList<CryptoCurrency>> {
            // TODO - improve
            if (adapter.itemCount == 0 && it.size > 0) {
                adapter.submitList(it)
                rv_crypto_currencies.scrollToPosition(0)
            } else {
                adapter.submitList(it)
            }
        })
        cryptoCurrenciesViewModel.networkState.observe(viewLifecycleOwner, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun initSwipeToRefresh() {
        cryptoCurrenciesViewModel.refreshState.observe(viewLifecycleOwner, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            cryptoCurrenciesViewModel.refresh()
        }
    }

    private fun navigate(symbol: String?) {
        if (symbol != null) {
            val action = CryptoCurrenciesFragmentDirections
                .actionCryptoCurrenciesScreenToCryptoCurrencyDetailsScreen(symbol)
            NavHostFragment.findNavController(this).navigate(action)
        }
    }
}
