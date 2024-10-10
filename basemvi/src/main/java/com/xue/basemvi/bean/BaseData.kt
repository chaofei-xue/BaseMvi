package com.xue.basemvi.bean

/**
 * Created by chaofei-xue
 * on 2024/10/10
 *
 * Description:
 * Basic data structure returned by the background
 * 后台返回的基本数据结构
 */
class BaseData<T> {
    var code: Int = -1
    var msg: String? = null
    var data: T? = null
}