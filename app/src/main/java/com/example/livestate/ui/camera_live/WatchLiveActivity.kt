package com.example.livestate.ui.camera_live

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.data.DataApp
import com.example.livestate.databinding.ActivityWatchLiveBinding
import com.example.livestate.model.CameraLiveModel
import com.example.livestate.utils.MapHelper
import com.example.livestate.widget.tap
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import java.text.NumberFormat
import java.util.Locale

class WatchLiveActivity : BaseActivity<ActivityWatchLiveBinding>() {
    private lateinit var adapter: CameraLiveAdapter
    private lateinit var list: List<CameraLiveModel>
    private lateinit var currentCameraLive: CameraLiveModel
    private lateinit var title: String
    private var address: String = "Location"
    private var youTubePlayer: YouTubePlayer? = null

    override fun setViewBinding(): ActivityWatchLiveBinding {
        return ActivityWatchLiveBinding.inflate(layoutInflater)
    }

    override fun initView() {
        title = intent.getStringExtra("title").toString()
        address = intent.getStringExtra("address").toString()
        currentCameraLive = DataApp.getListCameraLive(this).find { it.title == title }!!
        adapter = CameraLiveAdapter {
            currentCameraLive = it
            setData()
            getLocation(it.title)
            loadListCameraLive()
        }

        binding.rcvCameraLive.layoutManager = LinearLayoutManager(this@WatchLiveActivity)
        binding.rcvCameraLive.adapter = adapter

        setData()
    }

    override fun viewListener() {
        binding.imgBack.tap { finish() }
    }

    override fun dataObservable() {

    }

    private fun setData() {
        val videoId = getYoutubeVideoId(currentCameraLive.url)
        Log.d("TAG", "setData: $videoId")

        if (youTubePlayer == null) {
            binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(player: YouTubePlayer) {
                    youTubePlayer = player
                    player.loadVideo(videoId, 0f)
                }
            })
        } else {
            youTubePlayer?.loadVideo(videoId, 0f)
        }

        binding.apply {
            tvCountry.text = currentCameraLive.title
            tvView.text = "${randomFrom1000To10000(
                if (currentCameraLive.hot) 12345 else 49,
                if (currentCameraLive.hot) 23456 else 999)} views"
            tvLocation.text = address
        }

        loadListCameraLive()

    }

    fun randomFrom1000To10000(min: Int, max: Int): String {
        val number = (min..max).random()
        return NumberFormat.getNumberInstance(Locale.US).format(number)
    }

    fun getYoutubeVideoId(videoUrl: String): String {
        val uri = Uri.parse(videoUrl)
        return uri.getQueryParameter("v") ?: videoUrl.substringAfterLast("/")
    }

    private fun loadListCameraLive() {
        list = DataApp.getListCameraLive(this)
            .filter { it.title != currentCameraLive.title }
        adapter.addList(list.toMutableList())
    }

    private fun getLocation(address: String){
        binding.tvLocation.text = getString(R.string.loading)
        MapHelper.forwardGeocode(address) { geoResult ->
            val resolvedAddress = if (geoResult != null) {
                val lat = geoResult.coordinates()[1]
                val lon = geoResult.coordinates()[0]
                "$lat, $lon"
            } else {
                "Unknown"
            }
            binding.tvLocation.text = resolvedAddress
        }
    }

}
