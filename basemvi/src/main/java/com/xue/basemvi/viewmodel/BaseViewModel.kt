package com.xue.basemvi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xue.basemvi.bean.BaseData
import com.xue.basemvi.bean.RequestErrorBean
import com.xue.basemvi.config.BaseMviConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Created by chaofei-xue
 * on 2024/10/10
 *
 * Description:
 * The base class for all ViewModel subclasses,
 * which implements network request processing and data binding.
 *
 * 所有ViewModel子类的基类，实现了网络请求的处理及数据绑定。
 */
open class BaseViewModel : ViewModel() {

    private var logTag = javaClass.simpleName

    /**
     * Loading status
     * 加载状态
     */
    private val _loadingFlow = MutableStateFlow(false)
    val loadingFlow: StateFlow<Boolean> = _loadingFlow

    /**
     * Error status
     * 异常处理
     */
    private val _errorFlow = MutableStateFlow(RequestErrorBean(-1, ""))
    val errorFlow: StateFlow<RequestErrorBean> = _errorFlow

    /**
     * Normal situation
     * 正常情况
     */
    private var _normalFlow = MutableStateFlow(true)
    val normalFlow: StateFlow<Boolean> = _normalFlow

    /**
     * Request data and bind the returned data to Mutable State Flow.
     * 请求数据，将返回的数据绑定到MutableStateFlow。
     *
     * @param isLogin Whether to rely on login status
     *                是否依赖登录状态
     * @param showLoading Whether to display loading
     *                    是否展示加载
     * @param request Real network request method
     *                真正网络请求的方法
     * @param error Error handling
     *              错误处理
     * @param modelFlow MutableStateFlow that needs to bind data
     *                  需要绑定数据的MutableStateFlow
     * */
    fun <T : Any> request(
        isLogin: Boolean = true,
        showLoading: Boolean = false,
        modelFlow: MutableStateFlow<T>? = null,
        error: suspend (RequestErrorBean) -> Unit = { errorBean -> _errorFlow.value = errorBean },
        request: suspend () -> BaseData<T>
    ) {
        BaseMviConfig.getLoginState()?.let {
            if (isLogin && !it) {
                return
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (showLoading) {
                _loadingFlow.value = true
            }
            val baseData: BaseData<T>
            try {
                baseData = request()
                if (baseData.code == 0) {
                    _normalFlow.value = true
                    baseData.data?.let {
                        modelFlow?.value = it
                    }
                } else {
                    baseData.msg?.let { error(RequestErrorBean(baseData.code, it)) }
                }
            } catch (e: Exception) {
                e.message?.let { error(RequestErrorBean(-1, it)) }
            } finally {
                if (showLoading) {
                    _loadingFlow.value = false
                }
            }
        }
    }

    /**
     * Request data and return ordinary objects through the success callback method.
     * 请求数据，通过success回调方法返回普通对象。
     *
     * @param isLogin Whether to rely on login status
     *                是否依赖登录状态
     * @param showLoading Whether to display loading
     *                    是否展示加载
     * @param request Real network request method
     *                真正网络请求的方法
     * @param error Error handling
     *              错误处理
     * @param success Callback method for successful request. In this method,
     * the object converted into which the information returned by the interface
     * is returned is returned.
     *
     * 请求成功的回调方法，在此方法中返回接口返回的信息转换成为的对象。
     * */
    fun <T : Any> request(
        isLogin: Boolean = true,
        showLoading: Boolean = false,
        request: suspend () -> BaseData<T>,
        error: suspend (RequestErrorBean) -> Unit = { errorBean -> _errorFlow.value = errorBean },
        success: suspend (t: T) -> Unit,
    ) {
        BaseMviConfig.getLoginState()?.let {
            if (isLogin && !it) {
                return
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (showLoading) {
                _loadingFlow.value = true
            }
            val baseData: BaseData<T>
            try {
                baseData = request()
                if (baseData.code == 0) {
                    _normalFlow.value = true
                    baseData.data?.let { value ->
                        success(value)
                    }
                } else {
                    baseData.msg?.let { error(RequestErrorBean(baseData.code, it)) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                e.message?.let {
                    error(
                        RequestErrorBean(
                            -1,
                            "Network connection failed, please check and try again."
                        )
                    )
                }
            } finally {
                if (showLoading) {
                    _loadingFlow.value = false
                }
            }
        }
    }

    /**
     * info log
     * info 日志
     */
    protected fun logInfo(text: String) {
        Log.i(logTag, text)
    }

    /**
     * error log
     * error 日志
     */
    protected fun logError(text: String) {
        Log.e(logTag, text)
    }
}