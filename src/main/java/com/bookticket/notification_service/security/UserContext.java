package com.bookticket.notification_service.security;

/**
 * Thread-local storage for user authentication context.
 * Used to propagate user ID from Kafka events to REST API calls.
 */
public class UserContext {
    private static final ThreadLocal<Long> USER_ID_CONTEXT = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID_CONTEXT.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_CONTEXT.get();
    }

    public static void clear() {
        USER_ID_CONTEXT.remove();
    }
}

