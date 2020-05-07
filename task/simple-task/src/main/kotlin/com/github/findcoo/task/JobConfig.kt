package com.github.findcoo.task

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class JobConfig(private val jobBuilderFactory: JobBuilderFactory, private val stepBuilderFactory: StepBuilderFactory) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(JobConfig::class.java)
  }

  @Bean
  fun helloTask(): Tasklet {
    return Tasklet { _, _ ->
      log.info("hello")
      RepeatStatus.FINISHED
    }
  }

  @Bean
  fun job2(helloTask: Tasklet): Job {
    return jobBuilderFactory.get("job1")
      .incrementer(RunIdIncrementer())
      .start(stepBuilderFactory.get("step1")
        .tasklet(helloTask)
        .build())
      .build()
  }

}