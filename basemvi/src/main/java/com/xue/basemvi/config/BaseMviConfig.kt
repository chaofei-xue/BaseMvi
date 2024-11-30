package com.xue.basemvi.config

import okhttp3.OkHttpClient

/**
 * Created by chaofei-xue
 * on 2024/10/10
 *
 * Description:
 * Configure the login state. It is best to call it after the application starts or after the login
 * is successful. It is used to intercept interface requests that require login state. When not
 * configured, login status is not checked by default.
 *
 * 配置登录态，最好在应用启动后或登录成功后调用，用于拦截需要登录态的接口请求。未配置时，默认不检查登录态。
 */
object BaseMviConfig {

    private val defClient by lazy { OkHttpClient() }
    private var isLogin: Boolean? = null
    private var client:OkHttpClient? = null

    fun getLoginState(): Boolean? {
        return isLogin
    }

    fun setLoginState(isLogin: Boolean) {
        BaseMviConfig.isLogin = isLogin
    }

    fun getOkHttpClient(): OkHttpClient{
        return client ?: defClient
    }

    fun setOkHttpClient(client:OkHttpClient) {
        this.client = client
    }
}