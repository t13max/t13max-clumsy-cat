import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * 可重入kotlin锁
 *
 * @Author t13max
 * @Date 16:43 2025/7/9
 */
class ReentrantMutex {

    private val mutex = Mutex()
    private var owner: CoroutineContext? = null
    private var holdCount = 0

    suspend fun lock() {
        val currentContext = coroutineContext
        if (owner == currentContext) {
            // 当前协程已经持有锁，重入
            holdCount++
            return
        }

        // 否则尝试获取锁
        mutex.lock()
        owner = currentContext
        holdCount = 1
    }

    suspend fun unlock() {
        val currentContext = coroutineContext
        if (owner != currentContext) {
            throw IllegalStateException("Attempt to unlock a mutex that is not locked by current coroutine")
        }

        holdCount--
        if (holdCount == 0) {
            owner = null
            mutex.unlock()
        }
    }

    suspend inline fun <T> withLock(block: () -> T): T {
        lock()
        try {
            return block()
        } finally {
            unlock()
        }
    }
}