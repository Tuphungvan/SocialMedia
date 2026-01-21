package com.aht.social.infrastructure.cache;

public class CacheKeyGenerator {

    private static final String PREFIX = "SOCIAL_MEDIA:";

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