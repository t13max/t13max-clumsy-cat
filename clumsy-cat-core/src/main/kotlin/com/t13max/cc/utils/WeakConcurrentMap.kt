import com.t13max.cc.ClumsyCatEngine
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
    // 缺失时如何创建 value 的函数
    private val createValue: (K) -> V
) {
    companion object {
        // 最大分段数 1 shl 16 = 1 * 2^16 = 65536
        private const val MAX_SEGMENTS = 1 shl 16
    }

    // 用于 hash 分段的偏移位
    private val segmentShift: Int

    // 用于 hash 分段的掩码
    private val segmentMask: Int

    // 分段数组 每段一个 Segment
    private val segments: Array<Segment>

    init {

        var size = 1
        var shift = 0
        // 限制最大值 不超过MAX_SEGMENTS
        var level = ClumsyCatEngine.inst().conf.concurrencyLevel.coerceAtMost(MAX_SEGMENTS)
        //找到大于等于level的最小2的幂
        while (size < level) {
            ++shift
            size = size shl 1
        }
        // 计算偏移位 根据key的hash定位到哪个segment
        segmentShift = 32 - shift
        // 掩码 size是2的幂 size-1一定全是1 掩码快速取模
        segmentMask = size - 1
        // 初始化每个 Segment
        segments = Array(size) { Segment() }
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
        //hash值
        var h = key.hashCode()
        //扰动 分布更均匀
        val hash = spread(h)
        // 映射到段索引
        val index = (hash ushr segmentShift) and segmentMask
        return segments[index]
    }

    //扰动函数
    private fun spread(h: Int): Int {
        return (h xor (h ushr 16)) and 0x7fffffff
    }

    // 每段内部实现
    private inner class Segment {

        // 段锁
        private val lock = ReentrantLock()

        // 弱引用 map todo atb key是强引用 一直没回收
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