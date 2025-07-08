package com.example.livestate.ui.language

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestate.R
import com.example.livestate.databinding.ItemLanguageBinding

import com.example.livestate.model.LanguageModel
import com.example.livestate.view.base.BaseAdapter

class LanguageAdapter(
    val context: Context,
    val onClick: (lang: LanguageModel) -> Unit
) : BaseAdapter<ItemLanguageBinding, LanguageModel>() {

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemLanguageBinding {
        return ItemLanguageBinding.inflate(inflater, parent, false)
    }

    override fun creatVH(binding: ItemLanguageBinding): RecyclerView.ViewHolder =
        LanguageVH(binding)

    inner class LanguageVH(binding: ItemLanguageBinding) : BaseVH<LanguageModel>(binding) {
        override fun onItemClickListener(data: LanguageModel) {
            super.onItemClickListener(data)
            onClick.invoke(data)
        }

        override fun bind(data: LanguageModel) {
            super.bind(data)
            binding.txtName.text = data.name

            when (data.code) {
                "en" -> binding.icLang.setImageResource(R.drawable.flag_en)
                "de" -> binding.icLang.setImageResource(R.drawable.flag_ger)
                "es" -> binding.icLang.setImageResource(R.drawable.flag_span)
                "fr" -> binding.icLang.setImageResource(R.drawable.flag_fra)
                "hi" -> binding.icLang.setImageResource(R.drawable.flag_hindi)
                "in" -> binding.icLang.setImageResource(R.drawable.flag_indonesia)
                "pt" -> binding.icLang.setImageResource(R.drawable.flag_port)
                "vi" -> binding.icLang.setImageResource(R.drawable.flag_vi)
                "ja" -> binding.icLang.setImageResource(R.drawable.flag_japan)
            }
            if (data.active) {
                binding.txtName.setTextColor(context.getColor(R.color.white))
                binding.layoutItem.setBackgroundResource(R.drawable.bg_item_language_select)
            }
            else {
                binding.txtName.setTextColor(context.getColor(R.color.cl_text_464646))
                binding.layoutItem.setBackgroundResource(R.drawable.bg_item_language_unselect)
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setCheck(code: String) {
        for (item in listData) {
            item.active = item.code == code
        }
        notifyDataSetChanged()
    }
}