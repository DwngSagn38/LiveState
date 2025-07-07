package com.example.livestate.ui.camera_live

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.livestate.R
import com.example.livestate.databinding.ItemCameraLiveBinding
import com.example.livestate.model.CameraLiveModel
import com.example.livestate.view.base.BaseAdapter
import java.text.NumberFormat
import java.util.Locale

class CameraLiveAdapter(
    private val onItemClick: (CameraLiveModel) -> Unit
): BaseAdapter<ItemCameraLiveBinding, CameraLiveModel>(){

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int,
    ): ItemCameraLiveBinding {
        return ItemCameraLiveBinding.inflate(inflater, parent, false)
    }

    override fun creatVH(binding: ItemCameraLiveBinding): RecyclerView.ViewHolder {
        return CameraLiveViewHolder(binding)
    }

    inner class CameraLiveViewHolder(binding: ItemCameraLiveBinding) : BaseVH<CameraLiveModel>(binding){
        override fun bind(item: CameraLiveModel) {
            binding.apply {
                tvCountry.text = item.title
                tvView.text = "${randomFrom1000To10000(
                    if (item.hot) 12345 else 49,
                    if (item.hot) 23456 else 999)} views"
                imgHot.visibility = if (item.hot) View.VISIBLE else View.GONE
                Glide.with(imgLive.context)
                    .load(getYoutubeThumbnailUrl(item.url))
                    .placeholder(R.drawable.ic_launcher_background) // ảnh khi đang load
                    .error(R.drawable.ic_launcher_background) // ảnh khi lỗi
                    .into(imgLive)
                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }

    }
}

fun getYoutubeThumbnailUrl(videoUrl: String): String {
    val videoId = videoUrl.substringAfter("v=")
    return "https://img.youtube.com/vi/$videoId/0.jpg"
}


fun randomFrom1000To10000(min: Int, max: Int): String {
    val number = (min..max).random()
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}
