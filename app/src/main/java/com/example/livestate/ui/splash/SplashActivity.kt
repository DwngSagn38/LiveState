package  com.example.livestate.ui.splash

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivitySplashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.media.MediaPlayer
import com.example.livestate.ui.language_start.LanguageStartActivity
import com.example.livestate.ui.main.MainActivity
import com.example.livestate.ui.weather_activity.WeatherActivityActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private val croutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun setViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {

        croutineScope.launch {
            delay(2000)
            startIntro()
        }
    }

    override fun viewListener() {

    }

    override fun dataObservable() {

    }

    private fun startIntro() {
        showActivity(MainActivity::class.java)
        finish()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
    }
}