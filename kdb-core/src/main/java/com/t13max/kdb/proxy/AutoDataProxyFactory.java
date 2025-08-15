package com.t13max.kdb.proxy;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author t13max
 * @since 13:22 2025/8/15
 */
@Log4j2
public class AutoDataProxyFactory {

    // 缓存代理类
    private static final ConcurrentHashMap<Class<?>, Class<?>> proxyClassCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target) {
        Class<?> targetClass = target.getClass();

        // 获取或生成代理类
        Class<?> proxyClass = proxyClassCache.computeIfAbsent(targetClass, cls -> {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(cls);
            enhancer.setCallback(new SetMethodInterceptor(target));
            return enhancer.createClass();
        });

        try {
            // 创建新实例
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(proxyClass);
            enhancer.setCallback(new SetMethodInterceptor(target));
            return (T) enhancer.create();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 拦截器
    static class SetMethodInterceptor implements MethodInterceptor {

        private final Object target;

        private final Map<Method, Object> originalMap = new HashMap<>();

        private volatile boolean commit;

        public SetMethodInterceptor(Object target) {
            this.target = target;
        }

        /**
         * 拦截器 增加 set方法缓存初始值
         * todo cxcm 暂时只支持基本数据类型 引用类型可能有问题
         *
         * @Author t13max
         * @Date 13:27 2025/8/15
         */
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (method.getName().startsWith("set") && args != null && args.length == 1) {
                if (commit) {
                    log.error("事务已经提交 无法再set");
                    return null;
                }
                log.info("自动实体增强逻辑调用! class={}, method={}, value={}", obj.getClass().getSimpleName(), method.getName(), args[0]);

                Object exist = originalMap.get(method);
                if (exist == null) {
                    Object originalValue = getOriginalValue(method);
                    originalMap.put(method, originalValue);
                }
            } else if (method.getName().equals("rollback")) {
                // 回滚所有缓存值
                for (Map.Entry<Method, Object> entry : originalMap.entrySet()) {
                    Method setMethod = entry.getKey();
                    Object originalValue = entry.getValue();
                    setMethod.invoke(target, originalValue);
                }
                originalMap.clear(); // 回滚后清空缓存
                log.info("rollback 完成 class={}", obj.getClass().getSimpleName());
                return true;
            } else if (method.getName().equals("commit")) {
                this.originalMap.clear();
                this.commit = true;
                log.info("commit 完成 class={}", obj.getClass().getSimpleName());
            }
            return proxy.invoke(target, args);
        }

        private Object getOriginalValue(Method method) throws Throwable {
            // 推测 get 方法名
            String fieldName = method.getName().substring(3); // 去掉 "set"
            String getMethodName = "get" + fieldName;

            Object oldValue = null;
            try {
                Method getMethod = target.getClass().getMethod(getMethodName);
                oldValue = getMethod.invoke(target);
            } catch (NoSuchMethodException ignored) {
                // 有些 boolean 可能用 isXxx
                try {
                    Method isMethod = target.getClass().getMethod("is" + fieldName);
                    oldValue = isMethod.invoke(target);
                } catch (NoSuchMethodException ignored2) {
                }
            }
            return oldValue;
        }
    }
}
