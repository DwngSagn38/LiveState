package com.example.livestate.ui.nearby_places

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.example.livestate.R
import com.example.livestate.databinding.ItemDetailPlaceBinding
import com.example.livestate.model.NearbyPlace
import com.example.livestate.model.PlacesModel

class PlaceAdapter(
    val context: Context,
    private val places: List<NearbyPlace>,
    val onCalling: (String) -> Unit,
    val onDuration: (String) -> Unit,
    private val onItemClick: (NearbyPlace) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(val binding: ItemDetailPlaceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemDetailPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun getItemCount(): Int = places.size

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        with(holder.binding) {
            tvName.text = place.name ?: getString(context, R.string.unknown)
            tvLocation.text = place.address ?: "N/A"
            tvOpen.text = when {
                place.openingHours.isNullOrBlank() -> getString(context, R.string.unknown_open)
                place.isOpen == true -> "Open (${place.openingHours})"
                else -> "Close (${place.openingHours})"
            }


            tvOpen.setTextColor(
                ContextCompat.getColor(
                    root.context,
                    if (place.isOpen == true) R.color.green else R.color.red
                )
            )
            tvCalling.text = place.phone ?: "Call"
            tvCalling.setOnClickListener {
                onCalling(place.phone ?: "")
            }
            tvDuration.setOnClickListener {
                onDuration(place.address ?: "")
            }

            root.setOnClickListener {
                onItemClick(place)
            }

        }
    }
}