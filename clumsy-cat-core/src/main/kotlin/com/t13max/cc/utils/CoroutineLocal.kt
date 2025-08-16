package com.t13max.cc.utils

import kotlin.coroutines.*

/**
 * 协程本地
 * 协程版的ThreadLocal
 *
 * @Author t13max
 * @Date 17:34 2025/7/9
 */
class CoroutineLocal<T>(private val default: (() -> T)? = null) {

    private class Key<T> : CoroutineContext.Key<Element<T>>

    private class Element<T>(
        override val key: CoroutineContext.Key<*>,
        var value: T
    ) : AbstractCoroutineContextElement(key)

    private val key = Key<T>()

    fun get(context: CoroutineContext): T? {
        val element = context[key] as? Element<T>
        return element?.value ?: default?.invoke()
    }

    suspend fun get(): T? = get(coroutineContext)

    fun update(value: T?, context: CoroutineContext): CoroutineContext {
        return if (value == null) context.minusKey(key)
        else context + Element(key, value)
    }
}