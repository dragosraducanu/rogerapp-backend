package com.dragos.rogerapp

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RogerappApplication

fun main(args: Array<String>) {
	val logger = KotlinLogging.logger {}

	logger.debug { "Hi there!" }
	runApplication<RogerappApplication>(*args)
}
