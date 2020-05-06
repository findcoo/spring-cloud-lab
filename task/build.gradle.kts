import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
  api("org.springframework.boot:spring-boot-starter-batch")
  api("org.springframework.cloud:spring-cloud-starter-task")
}

subprojects {
  dependencies {
    implementation(project(":task"))

    testImplementation("org.springframework.batch:spring-batch-test")
  }

  tasks.getByName<Jar>("jar"){
    enabled = false
  }

  tasks.getByName<BootJar>("bootJar"){
    enabled = true
  }

  val bootJar = tasks.getByName<BootJar>("bootJar")

  docker {
    val dockerRegistry: String by rootProject.extra
    name = "findcoo/${project.name}:${rootProject.version}"
    tag("latest", "findcoo/${project.name}:latest")
    setDockerfile(file("$rootDir/Dockerfile"))
    files(bootJar.archiveFile)
    buildArgs(mapOf("JAR_FILE" to bootJar.archiveFileName.get()))
  }
}

val dockerPublish: Task by tasks.creating {
  subprojects {
    dependsOn(tasks.docker)
    dependsOn(tasks.dockerPush)
    dependsOn(tasks.dockerTagsPush)
  }
}

tasks.getByName<Jar>("jar"){
  enabled = false
}

tasks.getByName<BootJar>("bootJar"){
  enabled = true
}
