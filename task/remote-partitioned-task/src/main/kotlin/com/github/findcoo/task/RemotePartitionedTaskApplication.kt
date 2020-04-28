package com.github.findcoo.task

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.task.configuration.EnableTask

@SpringBootApplication
@EnableBatchProcessing
@EnableTask
class RemotePartitionedTaskApplication

fun main(args: Array<String>) {
  runApplication<RemotePartitionedTaskApplication>(*args)
}
