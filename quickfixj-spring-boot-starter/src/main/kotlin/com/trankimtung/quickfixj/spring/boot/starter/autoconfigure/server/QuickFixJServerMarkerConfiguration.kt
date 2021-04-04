package com.trankimtung.quickfixj.spring.boot.starter.autoconfigure.server

import org.springframework.context.annotation.Bean

/**
 * Marker for QuickFix/J Server auto configuration.
 */
class QuickFixJServerMarkerConfiguration {

    @Bean
    fun quickFixJServerAutoConfigurationMarker() = Marker()

    class Marker
}