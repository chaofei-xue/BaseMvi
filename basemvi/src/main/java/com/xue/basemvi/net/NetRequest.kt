package com.xue.basemvi.net

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Created by chaofei-xue
 * on 2024/10/10
 *
 * Description:
 * Process network requests, based on OkHttpClient. If the requirements are not met,
 * you can write this class yourself. As long as the return value is of String type,
 * it can be used with BaseRepository.
 *
 * 描述：
 * 处理网络请求，基于OkHttpClient，如果不满足要求，可以自己编写此类，
 * 只要返回值是String类型即可与BaseRepository搭配使用。
 */
object NetRequest {

    private val client by lazy { OkHttpClient() }
    private val gson by lazy { Gson() }

    /**
     * @param url Request url
     *            请求地址
     * @param type Request type
     *             请求类型
     * @param param Request parameters (pass parameters in the request body)
     *              请求参数（在请求体中传递参数）
     * @param query Request parameters (passing parameters in the URL)
     *              请求参数（在 URL 中传递参数）
     */
    suspend fun performRequest(
        url: String,
        type: RequestType,
        param: Any? = null,
        query: Map<String, Any> = mapOf(),
    ): String = withContext(Dispatchers.IO) {

        url.toHttpUrlOrNull()?.newBuilder()?.let { urlBuilder ->
            query.forEach { (key, value) ->
                urlBuilder.addQueryParameter(key, value.toString())
            }
            val request = Request.Builder()
                .url(urlBuilder.build())
                .method(
                    type.name, when (type) {
                        RequestType.GET -> null
                        else -> createRequestBody(param)
                    }
                ).build()

            client.newCall(request).execute().use { response ->
                return@withContext response.body?.string() ?: ""
            }
        }
        return@withContext ""
    }

    private fun createRequestBody(param: Any?): RequestBody {
        val jsonString = gson.toJson(param)
        return jsonString.toRequestBody("application/json".toMediaTypeOrNull())
    }
}

enum class RequestType {
    GET,
    POST,
    PUT,
    DELETE
}