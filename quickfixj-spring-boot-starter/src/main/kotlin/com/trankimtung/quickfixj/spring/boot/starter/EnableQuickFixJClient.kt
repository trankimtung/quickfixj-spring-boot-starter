package com.trankimtung.quickfixj.spring.boot.starter

import com.trankimtung.quickfixj.spring.boot.starter.autoconfigure.client.QuickFixJClientMarkerConfiguration
import org.springframework.context.annotation.Import
import java.lang.annotation.Inherited

/**
 * Enable QuickFix/J Client.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@Import(QuickFixJClientMarkerConfiguration::class)
annotation class EnableQuickFixJClient