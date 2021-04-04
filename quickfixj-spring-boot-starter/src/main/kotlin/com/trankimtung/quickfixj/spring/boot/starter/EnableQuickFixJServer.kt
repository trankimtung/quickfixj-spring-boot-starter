package com.trankimtung.quickfixj.spring.boot.starter

import com.trankimtung.quickfixj.spring.boot.starter.autoconfigure.server.QuickFixJServerMarkerConfiguration
import org.springframework.context.annotation.Import
import java.lang.annotation.Inherited

/**
 * Enable QuickFix/J Server.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@Import(QuickFixJServerMarkerConfiguration::class)
annotation class EnableQuickFixJServer