package com.trankimtung.quickfixj.spring.boot.starter.connection

enum class MessageStoreFactoryType {
    NOOP,
    JDBC,
    MEMORY,
    SLEEPY_CAT,
    FILE,
    CACHED_FILE
}