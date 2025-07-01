package com.example.livestate.ui.nearby_places

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.data.DataApp
import com.example.livestate.databinding.ActivityNearbyPlacesBinding
import com.example.livestate.model.PlacesModel
import com.example.livestate.widget.gone
import com.example.livestate.widget.invisible
import com.example.livestate.widget.tap
import com.example.livestate.widget.visible

class NearbyPlacesActivity : BaseActivity<ActivityNearbyPlacesBinding>() {
    private lateinit var adapter: NearbyPlacesAdapter
    private var isSearch = false
    private lateinit var listPlaces: List<PlacesModel>

    override fun setViewBinding(): ActivityNearbyPlacesBinding {
        return ActivityNearbyPlacesBinding.inflate(layoutInflater)
    }

    override fun initView() {
        listPlaces = DataApp.getListPlaces(this)
        setData(listPlaces)
        binding.imgBack.tap { finish() }
    }

    override fun viewListener() {
        binding.apply {
            imgSearch.tap {
                isSearch = !isSearch
                if (isSearch){
                    imgBack2.invisible()
                    llSearch.visible()
                }else{
                    imgBack2.gone()
                    llSearch.invisible()
                    edtSearch.setText("")
                }
            }
        }

        binding.edtSearch.addTextChangedListener { text: Editable? ->
            val query = text.toString().trim().lowercase()
            val allPlaces = DataApp.getListPlaces(this)
            val filtered = if (query.isEmpty()) {
                allPlaces
            } else {
                allPlaces.filter {
                    it.name.lowercase().contains(query)
                }
            }
            setData(filtered)
        }

    }

    override fun dataObservable() {
    }

    private fun setData(list: List<PlacesModel>){
        adapter = NearbyPlacesAdapter(){
            val intent = Intent(this, NearbyPlacesDetailActivity::class.java)
            intent.putExtra("idPlace", it.id)
            startActivity(intent)
        }
        binding.rcvPlace.layoutManager = GridLayoutManager(this,3)
        binding.rcvPlace.adapter = adapter

        adapter.addList(list.toMutableList())
    }

}