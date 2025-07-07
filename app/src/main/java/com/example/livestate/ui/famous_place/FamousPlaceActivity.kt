package com.example.livestate.ui.famous_place

import android.content.Intent
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
import com.example.livestate.databinding.ActivityFamousPlaceBinding
import com.example.livestate.model.FamousPlaceModel
import com.example.livestate.sharePreferent.PreferenceManager
import com.example.livestate.widget.invisible
import com.example.livestate.widget.tap
import com.example.livestate.widget.visible

class FamousPlaceActivity : BaseActivity<ActivityFamousPlaceBinding>() {
    private lateinit var adapter: FamousPlaceAdapter
    private lateinit var pref : PreferenceManager
    private lateinit var list : List<FamousPlaceModel>
    private var isFavorite = false

    override fun setViewBinding(): ActivityFamousPlaceBinding {
        return ActivityFamousPlaceBinding.inflate(layoutInflater)
    }

    override fun initView() {
        pref = PreferenceManager(this)
        list = DataApp.getListWithFavorite(this)
        setData(list)
    }

    override fun viewListener() {
        binding.imgBack.tap { finish() }
        binding.imgFavorite.setOnClickListener {
            isFavorite = !isFavorite
            if (isFavorite){
                binding.imgFavorite.setImageResource(R.drawable.img_heart_all)
                list = list.filter { it.favorite }
                setData(list)
                if (list.isEmpty()){
                    binding.rcvPlace.invisible()
                    binding.tvNotFound.visible()
                }
            }else{
                binding.imgFavorite.setImageResource(R.drawable.img_heart)
                list = DataApp.getListWithFavorite(this)
                setData(list)
            }

        }
    }

    override fun dataObservable() {
    }

    private fun setData(list : List<FamousPlaceModel>) {
        binding.rcvPlace.visible()
        binding.tvNotFound.invisible()
        adapter = FamousPlaceAdapter(
            onItemClick = {
                val intent = Intent(this, FamousDetailActivity::class.java)
                intent.putExtra("id", it.id)
                startActivity(intent)
            },
            onFavoriteClick = {
                it.favorite = !it.favorite
                if (it.favorite) {
                    pref.saveFavoriteId( it.id.toString())
                } else {
                    pref.removeFavoriteId( it.id.toString())
                }
                adapter.notifyDataSetChanged()
            }
        )

        Log.d("FamousPlaceActivity", "setData: $list")

        binding.rcvPlace.layoutManager = LinearLayoutManager(this)
        binding.rcvPlace.adapter = adapter

        adapter.addList(list.toMutableList())
    }

    override fun onResume() {
        super.onResume()
        list = DataApp.getListWithFavorite(this)
        setData(list)
    }

}