package com.example.livestate.ui.world_clock

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.livestate.R
import com.example.livestate.model.RestCountry
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CountryAdapter(private val originalList: List<RestCountry>) :
    RecyclerView.Adapter<CountryAdapter.CountryViewHolder>(), Filterable {

    private var filteredList: List<RestCountry> = originalList

    inner class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFlag: ImageView = itemView.findViewById(R.id.imgFlag)
        val txtCountry: TextView = itemView.findViewById(R.id.txtCountry)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val txtGmt: TextView = itemView.findViewById(R.id.tv_gmt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_country_time, parent, false)
        return CountryViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country = filteredList[position]
        holder.txtCountry.text = country.name.common

        val timezoneString = country.timezones.firstOrNull() ?: "UTC"
        val offsetDisplay = timezoneString.replace("UTC", "GMT")
        val currentTime = getCurrentTimeInOffset(timezoneString)
        holder.txtTime.text = currentTime
        holder.txtGmt.text = offsetDisplay
        Glide.with(holder.itemView.context)
            .load(country.flags.png)
            .transform(CropCircleWithBorderTransformation(3, ContextCompat.getColor(holder.itemView.context, R.color.color_EEEEEE)))
            .into(holder.imgFlag)

    }

    override fun getItemCount(): Int = filteredList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(query: CharSequence?): FilterResults {
                val filtered = if (query.isNullOrEmpty()) {
                    originalList
                } else {
                    originalList.filter {
                        it.name.common.contains(query.toString(), ignoreCase = true)
                    }
                }

                return FilterResults().apply {
                    values = filtered
                }
            }

            override fun publishResults(query: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<RestCountry>
                notifyDataSetChanged()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTimeInOffset(offsetString: String): String {
        return try {
            val zoneId = java.time.ZoneId.of(offsetString)
            java.time.ZonedDateTime.now(zoneId)
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            "--:--"
        }
    }
}
