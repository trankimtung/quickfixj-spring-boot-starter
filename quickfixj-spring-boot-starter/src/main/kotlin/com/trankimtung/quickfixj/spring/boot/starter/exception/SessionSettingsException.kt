package com.trankimtung.quickfixj.spring.boot.starter.exception

class SessionSettingsException(
    message: String?,
    throwable: Throwable?
) : QuickFixJException(message, throwable) {

    constructor(message: String?) : this(message, null)

    constructor() : this(null, null)
}