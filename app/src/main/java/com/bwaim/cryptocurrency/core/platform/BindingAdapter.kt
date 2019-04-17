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

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bwaim.cryptocurrency.R
import com.bwaim.cryptocurrency.core.extension.formatToString
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrencyJoin


@BindingAdapter("cryptoCurrency")
fun setText(textView: TextView, cryptoCurrencyJoin: CryptoCurrencyJoin?) {
    cryptoCurrencyJoin?.let {
        textView.text = when (textView.id) {
            R.id.name -> {
                textView.resources.getString(
                    R.string.currency_name_format,
                    cryptoCurrencyJoin.cryptoCurrency.fullName,
                    cryptoCurrencyJoin.cryptoCurrency.symbol
                )
            }
            R.id.volume -> {
                textView.resources.getString(
                    R.string.volume_label,
                    cryptoCurrencyJoin.cryptoCurrencyDetail.currentVolume.formatToString("##0.00"),
                    cryptoCurrencyJoin.cryptoCurrency.symbol
                )
            }
            R.id.price -> {
                textView.resources.getString(
                    R.string.price_label,
                    cryptoCurrencyJoin.cryptoCurrency.price.formatToString("##0.000")
                )
            }
            else -> null
        }
    }
}

@BindingAdapter("android:src")
fun setImage(imageView: ImageView, url: String?) {
    url?.let {
        val baseImgUrl = "https://www.cryptocompare.com/"
        GlideApp.with(imageView).load("$baseImgUrl$url")
            .centerCrop()
            .into(imageView)
    }
}