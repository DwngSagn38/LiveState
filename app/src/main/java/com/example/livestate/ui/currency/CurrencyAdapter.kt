package com.example.livestate.ui.currency


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestate.databinding.ItemCurrencyBinding
import com.example.livestate.model.Currency
import com.example.livestate.ui.currency.OnCurrencyClickListener
import com.example.livestate.widget.tap

class CurrencyAdapter(
    private var currencyList: MutableList<Currency>,
    private val listener: OnCurrencyClickListener
): RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHoder>() {

    private var filteredList: MutableList<Currency> = currencyList.toMutableList()

    inner class CurrencyViewHoder(private val binding: ItemCurrencyBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(currency: Currency) {
            binding.root.tap {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && position >= 0) {
                    listener.onItemClick(currency)
                    updateSelection(position)
                } else {
                    Log.e("CurrencyAdapter", "Vị trí không hợp lệ: $position")
                }
            }

            binding.tvOption.text = currency.content
            if (currency.isSelected) {
                binding.checkboxSelected.visibility = View.VISIBLE
                binding.checkboxNoSelected.visibility = View.GONE
            } else {
                binding.checkboxSelected.visibility = View.GONE
                binding.checkboxNoSelected.visibility = View.VISIBLE
            }
        }
    }
    fun updateList(newList: MutableList<Currency>) {
        currencyList = newList
        notifyDataSetChanged()
    }
    fun filterList(query: String) {
        filteredList = if (query.isEmpty()) {
            currencyList.toMutableList()
        } else {
            currencyList.filter {
                it.content.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun updateSelection(selectedPosition: Int) {
        if (selectedPosition >= 0 && selectedPosition < filteredList.size) {
            val selectedCurrency = filteredList[selectedPosition]
            val previousSelectedPosition = currencyList.indexOfFirst { it.isSelected }
            currencyList = currencyList.map {
                it.copy(isSelected = it == selectedCurrency)
            }.toMutableList()
            filterList("")
            if (previousSelectedPosition >= 0) {
                val previousFilteredPosition = filteredList.indexOfFirst {
                    it == currencyList[previousSelectedPosition]
                }
                if (previousFilteredPosition >= 0) notifyItemChanged(previousFilteredPosition)
            }
            val newFilteredPosition = filteredList.indexOfFirst {
                it == selectedCurrency
            }
            if (newFilteredPosition >= 0) notifyItemChanged(newFilteredPosition)
        } else {
            Log.e("CurrencyAdapter", "Vị trí không hợp lệ: $selectedPosition")
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHoder {
        val binding = ItemCurrencyBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CurrencyViewHoder(binding)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: CurrencyViewHoder, position: Int) {
        holder.bind(filteredList[position])
    }
}