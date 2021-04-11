package com.trankimtung.quickfixj.spring.boot.starter

import com.trankimtung.quickfixj.spring.boot.starter.exception.QuickFixJException
import com.trankimtung.quickfixj.spring.boot.starter.exception.SessionSettingsException
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer
import org.springframework.boot.diagnostics.FailureAnalysis

/**
 *  Failure analyzer for [QuickFixJException].
 */
class QuickFixJAutoConfigureFailureAnalyzer : AbstractFailureAnalyzer<QuickFixJException>() {

    override fun analyze(rootFailure: Throwable, cause: QuickFixJException): FailureAnalysis {
        var description = cause.message
        var action = cause.message

        when (cause) {
            is SessionSettingsException -> {
                description = "QuickFix/J Configuration Error."
                action = "Ensure QuickFix/J configuration file is readable and configured as documented here: " +
                        "https://www.quickfixj.org/usermanual/2.1.0/usage/configuration.html"
            }
        }

        return FailureAnalysis(description, action, getRootCause(cause))
    }

    private fun getRootCause(exception: Throwable): Throwable {
        var cause = exception
        while (cause.cause != null) {
            cause = cause.cause!!
        }
        return cause
    }
}