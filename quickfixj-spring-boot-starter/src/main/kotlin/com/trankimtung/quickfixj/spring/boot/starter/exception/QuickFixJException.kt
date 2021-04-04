package com.trankimtung.quickfixj.spring.boot.starter.exception

import java.lang.RuntimeException

open class QuickFixJException(
    message: String?,
    throwable: Throwable?
) : RuntimeException(message, throwable) {

    constructor(message: String) : this(message, null)

    constructor() : this(null, null)
}