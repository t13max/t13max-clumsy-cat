import java.lang.ref.WeakReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 分段锁 弱引用 线程安全 Map 实现
 *
 * @Author t13max
 * @Date 11:51 2025/7/9
 */
class WeakConcurrentMap<K : Any, V : Any>(
    // 并发段数量
    concurrencyLevel: Int = 16,
    // 缺失时如何创建 value 的函数
    private val createValue: (K) -> V
) {
    companion object {
        // 最大分段数
        private const val MAX_SEGMENTS = 1 shl 16
    }

    // 用于 hash 分段的偏移位
    private val segmentShift: Int

    // 用于 hash 分段的掩码
    private val segmentMask: Int

    // 分段数组 每段一个 Segment
    private val segments: Array<Segment>

    init {
        var ssize = 1
        var sshift = 0
        // 限制最大值
        var level = concurrencyLevel.coerceAtMost(MAX_SEGMENTS)
        while (ssize < level) {
            ++sshift
            ssize = ssize shl 1
        }
        // 计算偏移位
        segmentShift = 32 - sshift
        // 掩码
        segmentMask = ssize - 1
        // 初始化每个 Segment
        segments = Array(ssize) { Segment() }
    }

    // 获取对应 key 的 value 如果没有就创建
    fun get(key: K): V {
        return segmentFor(key).get(key)
    }

    // 判断 key 是否存在并未被 GC 回收
    fun contains(key: K): Boolean {
        return segmentFor(key).contains(key)
    }

    // 计算所有 segment 中存活的 entry 数量
    fun size(): Int {
        return segments.sumOf { it.size() }
    }

    // 根据 hash 分配 key 所在的 Segment
    private fun segmentFor(key: K): Segment {
        var h = key.hashCode()
        h += (h shl 15).xor(0xffffcd7d.toInt()) // 扰动函数
        h = h xor (h ushr 10)
        h += (h shl 3)
        h = h xor (h ushr 6)
        h += (h shl 2) + (h shl 14)
        val hash = h xor (h ushr 16) // 最终 hash
        val index = (hash ushr segmentShift) and segmentMask // 映射到段索引
        return segments[index]
    }

    // 每段内部实现
    private inner class Segment {
        // 段锁
        private val lock = ReentrantLock()

        // 弱引用 map
        private val map = mutableMapOf<K, WeakReference<V>>()

        // 获取值 如果值已 GC 则创建新值
        fun get(key: K): V {
            lock.withLock {
                val ref = map[key]
                val existing = ref?.get()
                // 如果没被 GC 直接返回
                if (existing != null) return existing
                // 创建新值
                val newValue = createValue(key)
                // 放入 map
                map[key] = WeakReference(newValue)
                return newValue
            }
        }

        // 判断值是否存在且未被 GC
        fun contains(key: K): Boolean {
            lock.withLock {
                return map[key]?.get() != null
            }
        }

        // 返回存活数量 并清理被 GC 的 key
        fun size(): Int {
            lock.withLock {
                val iter = map.entries.iterator()
                while (iter.hasNext()) {
                    if (iter.next().value.get() == null) {
                        iter.remove()
                    }
                }
                return map.size
            }
        }
    }
}