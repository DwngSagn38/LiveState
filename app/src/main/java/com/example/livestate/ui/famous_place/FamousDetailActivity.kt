package com.example.livestate.ui.famous_place

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.data.DataApp
import com.example.livestate.databinding.ActivityFamousDetailBinding
import com.example.livestate.sharePreferent.PreferenceManager
import com.example.livestate.widget.tap

class FamousDetailActivity : BaseActivity<ActivityFamousDetailBinding>() {
    private lateinit var pref : PreferenceManager
    private var isFavorite = false
    private var id = 0
    override fun setViewBinding(): ActivityFamousDetailBinding {
        return ActivityFamousDetailBinding.inflate(layoutInflater)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView() {
        pref  = PreferenceManager(this)

        id = intent.getIntExtra("id", 0)
        val data = DataApp.getListWithFavorite(this).find { it.id == id }
        if (data != null){
            binding.tvName.text = data.name
            setIconFavorite(data.favorite)
            isFavorite = data.favorite
            val bmp = BitmapFactory.decodeResource(resources, data.image)
            binding.horizontalImage.setImageBitmap(bmp)
        }


    }

    override fun viewListener() {
        binding.imgBack.tap { finish() }
        binding.imgFavorite.setOnClickListener {
            isFavorite = !isFavorite
            setIconFavorite(isFavorite)
            setFavorite(isFavorite)
        }
    }

    override fun dataObservable() {
    }

    private fun setIconFavorite(isFavorite: Boolean) {
        binding.imgFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_heart_select
            else R.drawable.ic_heart
        )
    }

    private fun setFavorite(isFavorite: Boolean) {
        if (isFavorite) {
            pref.saveFavoriteId( id.toString())
        } else {
            pref.removeFavoriteId( id.toString())
        }
    }

}