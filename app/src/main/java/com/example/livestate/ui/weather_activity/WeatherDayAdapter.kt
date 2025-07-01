package com.example.livestate.ui.weather_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.livestate.R
import com.example.livestate.model.WeatherDay

class WeatherDayAdapter(private val items: List<WeatherDay>) :
    RecyclerView.Adapter<WeatherDayAdapter.WeatherDayViewHolder>() {

    inner class WeatherDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val ivWeather: ImageView = itemView.findViewById(R.id.iv_wearthe)
        val tvTemp: TextView = itemView.findViewById(R.id.tv_temperature)
        val tvType: TextView = itemView.findViewById(R.id.tv_weather_type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherDayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather_day, parent, false)
        return WeatherDayViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherDayViewHolder, position: Int) {
        val item = items[position]
        holder.tvDate.text = item.date
        holder.tvTemp.text = item.temperatureText
        holder.tvType.text = item.weatherType
        holder.ivWeather.setImageResource(item.iconResId)
    }

    override fun getItemCount(): Int = items.size
}
