package com.rusty.openaiapigps.config.datasource;

public class DataSourceContextHolder {
    private static final ThreadLocal<LookupKey> context = new ThreadLocal<>();

    private DataSourceContextHolder() {
    }

    public static void setRoutingKey(LookupKey lookupKey) {
        clear();
        context.set(lookupKey);
    }

    public static LookupKey getRoutingKey() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}