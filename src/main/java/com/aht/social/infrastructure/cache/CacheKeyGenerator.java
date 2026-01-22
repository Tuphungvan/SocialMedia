package com.aht.social.infrastructure.cache;

/**
 * Utility class để generate cache keys theo convention
 */
public final class CacheKeyGenerator {

    private static final String PREFIX = "SOCIAL_MEDIA:";

    // Private constructor để prevent instantiation
    private CacheKeyGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getUserKey(String userId) {
        return PREFIX + "USER:" + userId;
    }

    public static String getPostKey(String postId) {
        return PREFIX + "POST:" + postId;
    }

    public static String getFeedKey(String userId) {
        return PREFIX + "FEED:" + userId;
    }

    public static String getFriendListKey(String userId) {
        return PREFIX + "FRIENDS:" + userId;
    }
}