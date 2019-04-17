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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bwaim.cryptocurrency.R
import com.bwaim.cryptocurrency.core.extension.formatToString
import com.bwaim.cryptocurrency.core.platform.GlideRequests
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrency


/**
 * A RecyclerView ViewHolder that displays a crypto currency.
 */
class CryptoCurrencyViewHolderView(
    view: View, private val glide: GlideRequests,
    private val navigateCallback: (String?) -> Unit
) : RecyclerView.ViewHolder(view) {

    private val logo: ImageView = view.findViewById(R.id.logo)
    private val name: TextView = view.findViewById(R.id.name)
    private val value: TextView = view.findViewById(R.id.value)
    private var cryptoCurrency: CryptoCurrency? = null
    private var increasedColor = ContextCompat.getColor(itemView.context, R.color.increasedValue)
    private var decreasedColor = ContextCompat.getColor(itemView.context, R.color.decreasedValue)
    private var normalColor = ContextCompat.getColor(itemView.context, android.R.color.black)

    init {
        view.setOnClickListener {
            navigateCallback(cryptoCurrency?.name)
        }
    }

    fun bind(cryptoCurrency: CryptoCurrency?) {
        cryptoCurrency?.let {
            this.cryptoCurrency = cryptoCurrency
            name.text = itemView.resources.getString(
                R.string.currency_name_format,
                cryptoCurrency.fullName, cryptoCurrency.symbol
            )
            formatValue(cryptoCurrency.price)
            glide.load("$BASE_IMG_URL${cryptoCurrency.imageUrl}")
                .centerCrop()
                .into(logo)
        }
    }

    /**
     * Called only when the item has changed. The new price is always different from the old one.
     */
    fun updateValue(item: CryptoCurrency?) {
        item?.let {
            val oldPrice = (cryptoCurrency?.price ?: 0.0)
            value.setTextColor(
                if (item.price > oldPrice) increasedColor
                else decreasedColor
            )
            itemView.postDelayed({ value.setTextColor(normalColor) }, HIGHLIGHT_DELAY)
        }
        cryptoCurrency = item

        formatValue(item?.price ?: 0.0)
    }

    private fun formatValue(newValue: Double) {
        value.text = itemView.resources.getString(
            R.string.price_format,
            newValue.formatToString("##0.000")
        )
    }

    companion object {
        private const val BASE_IMG_URL = "https://www.cryptocompare.com/"
        private const val HIGHLIGHT_DELAY = 20000L

        fun create(
            parent: ViewGroup,
            glide: GlideRequests,
            navigateCallback: (String?) -> Unit
        ): CryptoCurrencyViewHolderView {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.crypto_currency_item, parent, false)
            return CryptoCurrencyViewHolderView(view, glide, navigateCallback)
        }
    }
}
