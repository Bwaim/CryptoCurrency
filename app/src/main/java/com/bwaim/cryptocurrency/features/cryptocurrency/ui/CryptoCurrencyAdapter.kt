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

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bwaim.cryptocurrency.R
import com.bwaim.cryptocurrency.core.platform.GlideRequests
import com.bwaim.cryptocurrency.features.cryptocurrency.repository.NetworkState
import com.bwaim.cryptocurrency.features.cryptocurrency.vo.CryptoCurrency

/**
 * A simple adapter implementation that shows Crypto Currencies.
 */
class CryptoCurrencyAdapter(
    private val glide: GlideRequests,
    private val navigateCallback: (String?) -> Unit,
    private val retryCallback: () -> Unit
) :
    PagedListAdapter<CryptoCurrency, RecyclerView.ViewHolder>(CRYPTO_CURRENCY_COMPARATOR) {

    private var networkState: NetworkState? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.crypto_currency_item -> (holder as CryptoCurrencyViewHolderView).bind(getItem(position))
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bindTo(networkState)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val item = getItem(position)
            (holder as CryptoCurrencyViewHolderView).updateValue(item)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.crypto_currency_item -> CryptoCurrencyViewHolderView.create(parent, glide, navigateCallback)
            R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.network_state_item
        } else {
            R.layout.crypto_currency_item
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    companion object {
        val CRYPTO_CURRENCY_COMPARATOR = object : DiffUtil.ItemCallback<CryptoCurrency>() {
            override fun areContentsTheSame(oldItem: CryptoCurrency, newItem: CryptoCurrency): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: CryptoCurrency, newItem: CryptoCurrency): Boolean =
                oldItem.id == newItem.id

            override fun getChangePayload(oldItem: CryptoCurrency, newItem: CryptoCurrency): Any? {
                return if (newItem.price != oldItem.price) newItem.price else null
            }
        }
    }
}
