package com.example.livestate.ui.famous_place

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestate.R
import com.example.livestate.databinding.ItemFamousPlaceBinding
import com.example.livestate.model.FamousPlaceModel
import com.example.livestate.view.base.BaseAdapter

class FamousPlaceAdapter(
    private val onItemClick: (FamousPlaceModel) -> Unit,
    private val onFavoriteClick: (FamousPlaceModel) -> Unit
): BaseAdapter<ItemFamousPlaceBinding, FamousPlaceModel>(){


    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int,
    ): ItemFamousPlaceBinding {
        return ItemFamousPlaceBinding.inflate(inflater, parent, false)
    }

    override fun creatVH(binding: ItemFamousPlaceBinding): RecyclerView.ViewHolder {
        return FamousPlaceViewHolder(binding)
    }

    inner class FamousPlaceViewHolder(binding: ItemFamousPlaceBinding) : BaseVH<FamousPlaceModel>(binding){
        override fun bind(item: FamousPlaceModel) {
            binding.apply {

                imgImage.setImageResource(item.image)
                tvName.text = item.name
                imgFavorite.setImageResource(
                    if (item.favorite) R.drawable.ic_heart_select
                    else R.drawable.ic_heart
                )
                imgFavorite.setOnClickListener {
                    onFavoriteClick(item)
                }
                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }

    }
}