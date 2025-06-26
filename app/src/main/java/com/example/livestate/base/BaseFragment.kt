package com.example.livestate.view.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.livestate.utils.SystemUtil

abstract class BaseFragment<T : ViewBinding> : Fragment() {
    protected lateinit var binding: T

    protected abstract fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): T
    protected abstract fun initView()
    protected abstract fun viewListener()
    protected abstract fun dataObservable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = setViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SystemUtil.setLocale(requireActivity())
        initView()
        viewListener()
        dataObservable()
    }
    protected fun hideKeyboard() {
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
        view?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            it.clearFocus()
        }
    }

}