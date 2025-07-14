package com.example.livestate.ui.camera_live

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.data.DataApp
import com.example.livestate.databinding.ActivityCameraLiveBinding
import com.example.livestate.dialog.CameraLiveBottomSheet
import com.example.livestate.model.CameraLiveModel
import com.example.livestate.utils.MapHelper
import com.example.livestate.widget.tap

class CameraLiveActivity : BaseActivity<ActivityCameraLiveBinding>() {

    private lateinit var adapter : CameraLiveAdapter
    private lateinit var listCameraLive: List<CameraLiveModel>
    private lateinit var listCameraLiveHot: List<CameraLiveModel>
    private var filtered: List<CameraLiveModel> = listOf()
    private var address : String = ""
    private var isHot : Boolean = false
    override fun setViewBinding(): ActivityCameraLiveBinding {
        return ActivityCameraLiveBinding.inflate(layoutInflater)
    }

    override fun initView() {
        listCameraLive = DataApp.getListCameraLive(this)
        listCameraLiveHot = DataApp.getListCameraLive(this).filter { it.hot }
        setListCameraLive(listCameraLive)
    }

    override fun viewListener() {
        binding.apply {
            imgBack.tap { finish() }
            btnNearYou.tap {
                isHot = false
                applyFilter()

                btnNearYou.setBackgroundResource(R.drawable.bg_camera_btn_select)
                btnHotSearch.setBackgroundResource(R.drawable.bg_camera_btn)
                btnNearYou.setTextColor(Color.WHITE)
                btnHotSearch.setTextColor(Color.parseColor("#457CBB"))
            }

            btnHotSearch.tap {
                isHot = true
                applyFilter()

                btnHotSearch.setBackgroundResource(R.drawable.bg_camera_btn_select)
                btnNearYou.setBackgroundResource(R.drawable.bg_camera_btn)
                btnHotSearch.setTextColor(Color.WHITE)
                btnNearYou.setTextColor(Color.parseColor("#457CBB"))
            }


            edtSearch.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    true
                } else {
                    false
                }
            }

            edtSearch.addTextChangedListener { text: Editable? ->
                val query = text.toString().trim().lowercase()
                val allPlaces = if (!isHot) listCameraLive else listCameraLiveHot
                filtered = if (query.isEmpty()) {
                    allPlaces
                } else {
                    allPlaces.filter { it.title.lowercase().contains(query) }
                }

                setListCameraLive(filtered)

                // Hiển thị "Not found" nếu không có kết quả
                tvNotFound.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                rcvCameraLive.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
            }

        }
    }

    override fun dataObservable() {

    }

    override fun onStop() {
        super.onStop()
    }

    private fun applyFilter() {
        val query = binding.edtSearch.text.toString().trim().lowercase()
        val sourceList = if (isHot) listCameraLiveHot else listCameraLive

        filtered = if (query.isEmpty()) {
            sourceList
        } else {
            sourceList.filter { it.title.lowercase().contains(query) }
        }

        setListCameraLive(filtered)

        binding.tvNotFound.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rcvCameraLive.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }


    private fun setListCameraLive(list: List<CameraLiveModel>) {
        adapter = CameraLiveAdapter { camera ->
            // Lấy địa chỉ bất đồng bộ và xử lý sau khi đã có lat, lon
            MapHelper.forwardGeocode(camera.title) { geoResult ->
                val resolvedAddress = if (geoResult != null) {
                    val lat = geoResult.coordinates()[1]
                    val lon = geoResult.coordinates()[0]
                    "$lat, $lon"
                } else {
                    "Unknown"
                }

                Log.d("TAG", "Resolved Address: $resolvedAddress")

                // Chỉ hiển thị BottomSheet sau khi đã có địa chỉ
                val bottomSheet = CameraLiveBottomSheet(resolvedAddress, camera) {
                    val intent = Intent(this, WatchLiveActivity::class.java)
                    intent.putExtra("title", it.title)
                    intent.putExtra("address", resolvedAddress)
                    startActivity(intent)
                }
                bottomSheet.show(supportFragmentManager, bottomSheet.tag)
            }
        }

        binding.rcvCameraLive.layoutManager = LinearLayoutManager(this)
        binding.rcvCameraLive.adapter = adapter
        adapter.addList(list.toMutableList())
    }


}