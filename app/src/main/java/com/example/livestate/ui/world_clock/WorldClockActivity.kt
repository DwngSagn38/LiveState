package com.example.livestate.ui.world_clock

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivityWorldClockBinding
import com.example.livestate.model.CountryTime
import kotlinx.coroutines.launch

class WorldClockActivity : BaseActivity<ActivityWorldClockBinding>() {
    override fun setViewBinding(): ActivityWorldClockBinding {
        return ActivityWorldClockBinding.inflate(layoutInflater)
    }

    private lateinit var countryAdapter: CountryAdapter

    override fun initView() {
        val recyclerView = binding.recyclerView
        val progressBar = binding.progressBar
        val loadingOverlay = binding.loadingOverlay

        recyclerView.visibility = View.GONE
        loadingOverlay.visibility = View.VISIBLE
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        lifecycleScope.launch {
            try {
                val countries = ApiClient.countryService.getAllCountries()
                countryAdapter = CountryAdapter(countries)
                recyclerView.adapter = countryAdapter

                recyclerView.visibility = View.VISIBLE
                loadingOverlay.visibility = View.GONE
            } catch (e: Exception) {
                Log.e("API_ERROR", "Lỗi: ${e.message}")
                loadingOverlay.visibility = View.GONE
                Toast.makeText(this@WorldClockActivity, "Không tải được dữ liệu", Toast.LENGTH_SHORT).show()
            }
        }

        // 👉 Tìm kiếm theo tên quốc gia
        binding.etCity.addTextChangedListener {
            val query = it.toString()
            countryAdapter.filter.filter(query)
        }
    }



    override fun viewListener() {
    }

    override fun dataObservable() {
    }

}