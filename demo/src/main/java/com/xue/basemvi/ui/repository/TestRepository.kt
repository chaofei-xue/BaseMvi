package com.xue.basemvi.ui.repository

import com.google.gson.reflect.TypeToken
import com.xue.basemvi.bean.BaseData
import com.xue.basemvi.net.NetRequest
import com.xue.basemvi.net.RequestType
import com.xue.basemvi.repository.BaseRepository

/**
 * Created by chaofei-xue
 * on 2024/10/10
 * Description：
 */
data class UserInfo(
    val userId: String = "",
    val userName: String = "",
    val avatar: String = "",
    val token: String = "",
)

class TestRepository : BaseRepository() {
    suspend fun login(userName: String, password: String): BaseData<UserInfo> {
        return executeRequest(object : TypeToken<BaseData<UserInfo>>() {}.type) {
            NetRequest.performRequest(
                "http://www.123456.com/login",
                RequestType.PUT,
                param = hashMapOf<String, Any>().apply {
                    put("userName", userName)
                    put("password", password)
                })
        }
    }
}