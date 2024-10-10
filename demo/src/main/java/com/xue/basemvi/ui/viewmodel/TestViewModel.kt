package com.xue.basemvi.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.xue.basemvi.ui.repository.TestRepository
import com.xue.basemvi.ui.repository.UserInfo
import com.xue.basemvi.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by chaofei-xue
 * on 2024/10/10
 * Description：
 */
data class TestUIState(
    val isLoginSuccess: Boolean = false,
    val userInfo: UserInfo = UserInfo(),
    val errorMsg: String = "",
)

class TestViewModel : BaseViewModel() {

    private val repository by lazy { TestRepository() }
    private val _uiState = MutableStateFlow(TestUIState())
    var uiState = _uiState.asStateFlow()

    fun login(userName: String, password: String) {
        viewModelScope.launch {
            request(
                isLogin = false,
                showLoading = true,
                request = { repository.login(userName, password) },
                error = {
                    _uiState.update { bean ->
                        bean.copy(
                            isLoginSuccess = false,
                            errorMsg = it.message
                        )
                    }
                }
            ) { result ->
                _uiState.update { bean -> bean.copy(isLoginSuccess = true, userInfo = result) }
            }
        }
    }
}