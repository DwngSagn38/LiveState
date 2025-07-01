package com.example.livestate.ui.nearby_places

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.livestate.databinding.ItemPlacesBinding
import com.example.livestate.model.PlacesModel
import com.example.livestate.view.base.BaseAdapter

class NearbyPlacesAdapter(
    private val onItemClick: (PlacesModel) -> Unit
): BaseAdapter<ItemPlacesBinding, PlacesModel>(){


    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int,
    ): ItemPlacesBinding {
        return ItemPlacesBinding.inflate(inflater, parent, false)
    }

    override fun creatVH(binding: ItemPlacesBinding): RecyclerView.ViewHolder {
       return NearbyPlacesViewHolder(binding)
    }

    inner class NearbyPlacesViewHolder(binding: ItemPlacesBinding) : BaseVH<PlacesModel>(binding){
        override fun bind(item: PlacesModel) {
            binding.apply {
                tvName.text = item.name
                imgIcon.setImageResource(item.image)
                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }

    }
}