package com.example.livestate.dialog

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.livestate.R
import com.example.livestate.databinding.BottomSheetCameraLiveBinding
import com.example.livestate.model.CameraLiveModel
import com.example.livestate.utils.MapHelper
import com.example.livestate.view.base.BaseBottomSheetDialog

class CameraLiveBottomSheet(
    private val address : String,
    private val model: CameraLiveModel,
    private val onItemClick: (CameraLiveModel) -> Unit
) : BaseBottomSheetDialog<BottomSheetCameraLiveBinding>() {

    override fun makeBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetCameraLiveBinding {
        return BottomSheetCameraLiveBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        binding.apply {
            tvCountry.text = model.title
            tvLocation.text = address

            imgHot.visibility = if (model.hot) View.VISIBLE else View.GONE

            Glide.with(imgLive.context)
                .load(getYoutubeThumbnailUrl(model.url))
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(imgLive)
            btnWatch.setOnClickListener {
                onItemClick(model)
                dismiss()
            }
            imgClose.setOnClickListener { dismiss() }
        }
    }
}


fun getYoutubeThumbnailUrl(videoUrl: String): String {
    val videoId = videoUrl.substringAfter("v=")
    return "https://img.youtube.com/vi/$videoId/0.jpg"
}