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
import com.bwaim.cryptocurrency.R
import com.bwaim.cryptocurrency.core.di.Injectable
import com.bwaim.cryptocurrency.core.extension.formatToString
import com.bwaim.cryptocurrency.core.extension.viewModel
import com.bwaim.cryptocurrency.databinding.FragmentCryptoCurrencyDetailsBinding
import javax.inject.Inject

class CryptoCurrencyDetailsFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var cryptoCurrencyDetailsViewModel: CryptoCurrencyDetailsViewModel

    private lateinit var cryptoCurrencyDetailsFragBinding: FragmentCryptoCurrencyDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        cryptoCurrencyDetailsFragBinding = FragmentCryptoCurrencyDetailsBinding.inflate(inflater, container, false)
        return cryptoCurrencyDetailsFragBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cryptoCurrencyDetailsViewModel = viewModel(viewModelFactory)
        cryptoCurrencyDetailsFragBinding.modelview = cryptoCurrencyDetailsViewModel
        cryptoCurrencyDetailsFragBinding.lifecycleOwner = viewLifecycleOwner
        if (arguments != null) {
            val args = CryptoCurrencyDetailsFragmentArgs.fromBundle(arguments!!)
            cryptoCurrencyDetailsViewModel.setData(args.symbol)
        }

        setObservers()
    }

    fun setObservers() {
        cryptoCurrencyDetailsViewModel.quantityInput.observe(viewLifecycleOwner, Observer { input ->
            input?.let { quantity ->
                if (quantity.isEmpty()) {
                    cryptoCurrencyDetailsFragBinding.buySimulation.text = ""
                } else {
                    cryptoCurrencyDetailsViewModel.cryptoCurrency.value?.let {
                        cryptoCurrencyDetailsFragBinding.buySimulation.text =
                            getString(
                                R.string.conversion_text,
                                quantity,
                                (quantity.toDouble() / it.cryptoCurrency.price).formatToString("##0.00"),
                                it.cryptoCurrency.symbol
                            )
                    }
                }
            }
        })
    }
}
