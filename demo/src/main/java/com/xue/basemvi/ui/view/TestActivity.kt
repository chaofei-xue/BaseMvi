package com.xue.basemvi.ui.view

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.xue.basemvi.databinding.ActivityTestBinding
import com.xue.basemvi.ui.viewmodel.TestViewModel
import com.xue.basemvi.view.BaseActivity
import kotlinx.coroutines.launch

/**
 * Created by chaofei-xue
 * on 2024/10/10
 * Description：
 */
class TestActivity: BaseActivity<ActivityTestBinding, TestViewModel>() {

    private var userName: String = ""
    private var password: String = ""

    override fun initData() {
        initView()
        refreshData()
    }

    private fun initView() {
        initListener()
    }

    private fun initListener() {
        rootView.btnLogin.setOnClickListener {
            viewModel.login(userName, password)
        }
        rootView.etPassword.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                password = s.toString()
                checkInputState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        rootView.etUserName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userName = s.toString()
                checkInputState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun refreshData() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect {

                }
            }
        }
    }

    private fun checkInputState() {
        rootView.btnLogin.isEnabled = userName.isNotEmpty() && password.isNotEmpty()
    }
}