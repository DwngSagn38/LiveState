package com.example.livestate.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivityMainBinding
import com.example.livestate.ui.setting.SettingActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun setViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {

    }

    override fun viewListener() {
        binding.imgSetting.setOnClickListener {
            showActivity(SettingActivity::class.java)
        }
    }

    override fun dataObservable() {
    }

}