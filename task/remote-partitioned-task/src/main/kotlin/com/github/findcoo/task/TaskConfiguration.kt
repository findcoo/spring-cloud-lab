package com.github.findcoo.task

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.partition.PartitionHandler
import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader
import org.springframework.cloud.deployer.spi.task.TaskLauncher
import org.springframework.cloud.task.batch.partition.DeployerPartitionHandler
import org.springframework.cloud.task.batch.partition.DeployerStepExecutionHandler
import org.springframework.cloud.task.batch.partition.PassThroughCommandLineArgsProvider
import org.springframework.cloud.task.batch.partition.SimpleEnvironmentVariablesProvider
import org.springframework.cloud.task.repository.TaskRepository
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import java.util.*


@Configuration
class TaskConfiguration(
  private val jobBuilderFactory: JobBuilderFactory,
  private val stepBuilderFactory: StepBuilderFactory,
  private val resourceLoader: DelegatingResourceLoader,
  private val context: ConfigurableApplicationContext,
  private val jobRepository: JobRepository,
  private val taskRepository: TaskRepository,
  private val environment: Environment
) {

  companion object {
    private const val GRID_SIZE: Int = 2
  }

  @Bean
  fun partitionHandler(taskLauncher: TaskLauncher, jobExplorer: JobExplorer): PartitionHandler {
    val resource = resourceLoader.getResource("docker://findcoo/remote-partitioned-task")
    val partitionHandler = DeployerPartitionHandler(taskLauncher, jobExplorer, resource, "workerStep", taskRepository)
    val commandLineArgs = arrayListOf<String>()

    commandLineArgs.add("--spring.profiles.active=worker")
    commandLineArgs.add("--spring.cloud.task.initialize.enable=false")
    commandLineArgs.add("--spring.batch.initializer.enabled=false")

    partitionHandler.setCommandLineArgsProvider(PassThroughCommandLineArgsProvider(commandLineArgs))
    partitionHandler.setEnvironmentVariablesProvider(SimpleEnvironmentVariablesProvider(environment))
    partitionHandler.setMaxWorkers(1)
    partitionHandler.setApplicationName("RemotePartitionedTask")

    return partitionHandler
  }

  @Bean
  @Profile("!worker")
  fun partitionedJob(partitionHandler: PartitionHandler): Job {
    return jobBuilderFactory.get("partitionedJob")
      .incrementer(RunIdIncrementer())
      .start(step1(partitionHandler))
      .build()
  }

  @Bean
  fun step1(partitionHandler: PartitionHandler): Step {
    return stepBuilderFactory.get("step1")
      .partitioner(workerStep().name, partitioner())
      .step(workerStep())
      .partitionHandler(partitionHandler)
      .build()
  }

  @Bean
  fun partitioner(): Partitioner {
    return Partitioner { gridSize ->
      val partitions: MutableMap<String, ExecutionContext> = HashMap(gridSize)
      for (i in 0 until GRID_SIZE) {
        val context1 = ExecutionContext()
        context1.put("partitionNumber", i)
        partitions["partition$i"] = context1
      }
      partitions
    }
  }

  @Bean
  @Profile("worker")
  fun stepExecutionHandler(jobExplorer: JobExplorer?): DeployerStepExecutionHandler? {
    return DeployerStepExecutionHandler(context, jobExplorer, jobRepository)
  }

  @Bean
  fun workerStep(): Step {
    return stepBuilderFactory["workerStep"]
      .tasklet(workerTasklet(null))
      .build()
  }

  @Bean
  @StepScope
  fun workerTasklet(
    @Value("#{stepExecutionContext['partitionNumber']}") partitionNumber: Int?): Tasklet {
    return Tasklet { _, _ ->
      println("This tasklet ran partition: $partitionNumber")
      RepeatStatus.FINISHED
    }
  }
}