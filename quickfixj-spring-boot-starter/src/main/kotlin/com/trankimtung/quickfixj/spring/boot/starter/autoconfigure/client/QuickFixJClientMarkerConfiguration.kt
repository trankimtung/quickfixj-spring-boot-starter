package com.trankimtung.quickfixj.spring.boot.starter.autoconfigure.client

import org.springframework.context.annotation.Bean

/**
 * Marker for QuickFix/J Client auto configuration.
 */
class QuickFixJClientMarkerConfiguration {

    @Bean
    fun quickFixJClientAutoConfigurationMarker() = Marker()

    class Marker
}