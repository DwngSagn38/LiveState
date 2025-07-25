package com.example.livestate.ui.intro.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.livestate.R
import com.example.livestate.databinding.LayoutItemIntroBinding

import com.example.livestate.model.IntroModel


@SuppressLint("UseCompatLoadingForDrawables")
open class Fragment4 : Fragment() {

    private lateinit var binding: LayoutItemIntroBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = LayoutItemIntroBinding.inflate(inflater, container, false)
        try {
            initView()
            viewListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    private fun initView() {
        try {
            val data = IntroModel(
                R.drawable.img_intro4, R.string.title_intro_4, R.string.content_4
            )

                binding.ivIntro.setImageResource(data.image)
                binding.tvTile.setText(data.content)
                binding.tvContent.setText(data.title)

        } catch (_: Exception) {

        }
    }

    private fun viewListener() {

    }
}