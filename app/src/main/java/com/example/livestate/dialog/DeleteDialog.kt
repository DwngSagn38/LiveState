package com.example.livestate.dialog

import android.app.Activity
import android.view.LayoutInflater
import com.example.livestate.databinding.DialogDeleteBinding
import com.example.livestate.base.BaseDialog
import com.example.livestate.widget.tap

class DeleteDialog (
    activity1: Activity,
    val content: String? = null,
    private var action: () -> Unit,
) : BaseDialog<DialogDeleteBinding>(activity1, true) {


    override fun getContentView(): DialogDeleteBinding {
        return DialogDeleteBinding.inflate(LayoutInflater.from(activity))
    }

    override fun initView() {
    }

    override fun bindView() {
        binding.root.tap { dismiss() }
        binding.apply {

            tvNo.tap {
                dismiss()
            }

            tvYes.tap {
                action.invoke()
                dismiss()
            }
        }
    }
}