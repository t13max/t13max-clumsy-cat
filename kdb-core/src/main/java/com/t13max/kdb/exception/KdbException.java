package com.t13max.kdb.exception;

/**
 * 异常
 *
 * @author t13max
 * @since 16:55 2025/7/7
 */
public class KdbException extends RuntimeException {

    public KdbException() {
    }

    public KdbException(String message) {
        super(message);
    }

    public KdbException(String message, Throwable cause) {
        super(message, cause);
    }

    public KdbException(Throwable cause) {
        super(cause);
    }

    public KdbException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * 重写 防止每次都生成堆栈信息
     *
     * @Author t13max
     * @Date 16:55 2025/7/7
     */
    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        super.setStackTrace(stackTrace);
    }
}
