package com.example.livestate.ui.earth3d

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivityTheEarthBinding
import com.example.livestate.widget.gone
import com.example.livestate.widget.tap
import com.example.livestate.widget.visible
import org.json.JSONObject

class TheEarthActivity : BaseActivity<ActivityTheEarthBinding>() {
    private lateinit var server: LocalWebServer
    private var isWebViewLoaded = false
    private var isMoon = false
    private var isChangeMap = false
    private var isLoading = false


    inner class JSInterface {
        @JavascriptInterface
        fun onLocationFetched() {
            runOnUiThread {
                loading(false) // Ẩn loading
                isLoading = true
            }
        }
    }

    override fun setViewBinding(): ActivityTheEarthBinding {
        return ActivityTheEarthBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val webView = WebView(this)
        val supportsWebGL = webView.settings.userAgentString.contains("Chrome")

        binding.webView.addJavascriptInterface(JSInterface(), "AndroidInterface")

        startServer()
    }

    override fun viewListener() {
        binding.edtSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                val keyword = binding.edtSearch.text.toString()
                if (keyword.isNotBlank()) {
                    val jsCode = "searchLocationFromAndroid(${JSONObject.quote(keyword)});"
                    binding.webView.evaluateJavascript(jsCode, null)

                    // Ẩn bàn phím sau khi tìm kiếm
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
                }

                true  // Đã xử lý sự kiện
            } else {
                false // Chưa xử lý, chuyển tiếp
            }
        }


        binding.imgMyLocation.tap {
            isLoading = false
            loading(true)

            binding.webView.evaluateJavascript("getMyLocation()", null)
        }

        binding.imgLight.tap {
            isMoon = !isMoon
            val jsCode = "toggleLight(${isMoon})"
            binding.webView.evaluateJavascript(jsCode, null)
            setIconLightMoon(isMoon)
        }

        binding.imgBack.tap { finish() }

        binding.imgAddZoom.setOnClickListener {
            binding.webView.evaluateJavascript("zoomIn();", null)
        }

        binding.imgMinusZoom.setOnClickListener {
            binding.webView.evaluateJavascript("zoomOut();", null)
        }

        binding.imgChange2D.tap {
            isChangeMap = !isChangeMap
            val jsCode = "setImageryStyle(${isChangeMap})"
            binding.webView.evaluateJavascript(jsCode, null)
        }
    }


    override fun dataObservable() {
    }


    private fun startServer() {
        // Start server
        server = LocalWebServer(this)
        try {
            server.start()
            Log.d("LocalWebServer", "Server started successfully")
        } catch (e: Exception) {
            Log.e("LocalWebServer", "Failed to start server", e)
        }

        val settings = binding.webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setSupportZoom(true)

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                callback?.invoke(origin, true, false)
            }
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("TheEarthActivity", "Loading URL http://127.0.0.1:8080/")
            binding.webView.loadUrl("http://127.0.0.1:8080/")
        }, 300)

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("TheEarthActivity", "WebView load hoàn tất")
                binding.webView.visible()
                binding.progressBar.gone()
                isWebViewLoaded = true
            }
        }

    }

    private fun setIconLightMoon(isMoon: Boolean) {
        if (isMoon){
            binding.imgLight.setImageResource(R.drawable.ic_moon)
        }else{
            binding.imgLight.setImageResource(R.drawable.ic_light)
        }
    }

    private fun loading(show: Boolean) {
        if (show) {
            binding.progressBar.visible()
        } else {
            binding.progressBar.gone()
        }
    }


}