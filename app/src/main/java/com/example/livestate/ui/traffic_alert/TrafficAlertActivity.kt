package com.example.livestate.ui.traffic_alert

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.livestate.R
import com.example.livestate.base.BaseActivity2
import com.example.livestate.databinding.ActivityTrafficAlertBinding
import com.example.livestate.utils.MapHelper

class TrafficAlertActivity : BaseActivity2<ActivityTrafficAlertBinding>() {
    override fun setViewBinding(): ActivityTrafficAlertBinding {
        MapHelper.initMapLibre(applicationContext)
        return ActivityTrafficAlertBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun viewListener() {

    }

    override fun dataObservable() {

    }

}