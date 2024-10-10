package com.xue.basemvi.repository

import com.google.gson.Gson
import com.xue.basemvi.bean.BaseData
import java.lang.reflect.Type

/**
 * Created by chaofei-xue
 * on 2024/10/10
 *
 * Description:
 * The base class of all Repository classes, converts the data returned by the request into objects.
 *
 * 所有Repository类的基类，将请求返回的数据转为对象。
 */
open class BaseRepository {
    suspend fun <T : Any> executeRequest(
        type: Type,
        block: suspend () -> String,
    ): BaseData<T> {
        val jsonStr = block.invoke()
        return Gson().fromJson(jsonStr, type)
    }
}

