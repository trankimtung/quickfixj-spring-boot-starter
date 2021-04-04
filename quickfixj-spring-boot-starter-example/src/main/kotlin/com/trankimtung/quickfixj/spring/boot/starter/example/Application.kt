package com.trankimtung.quickfixj.spring.boot.starter.example

import com.trankimtung.quickfixj.spring.boot.starter.EnableQuickFixJServer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableQuickFixJServer
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}