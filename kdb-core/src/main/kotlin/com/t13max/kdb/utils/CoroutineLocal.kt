package com.t13max.kdb.utils

import kotlin.coroutines.*

/**
 * 协程本地
 *
 * @Author t13max
 * @Date 17:34 2025/7/9
 */
class CoroutineLocal<T>(private val default: () -> T) {

    private class CoroutineLocalKey<T> : CoroutineContext.Key<CoroutineLocalElement<T>>

    private class CoroutineLocalElement<T>(
        override val key: CoroutineContext.Key<*>,
        var value: T
    ) : AbstractCoroutineContextElement(key)

    private val key = CoroutineLocalKey<T>()

    suspend fun get(): T {
        val ctx = coroutineContext
        return get(ctx)
    }

    fun get(context: CoroutineContext): T {
        val element = context[key]
        return if (element is CoroutineLocalElement<*>) {
            @Suppress("UNCHECKED_CAST")
            element.value as T
        } else {
            default()
        }
    }

    fun update(value: T?, context: CoroutineContext): CoroutineContext {
        return if (value == null) {
            context.minusKey(key)
        } else {
            context + CoroutineLocalElement(key, value)
        }
    }
}