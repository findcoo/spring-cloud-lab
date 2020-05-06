import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.net.URI


plugins {
  id("maven")
  id("org.springframework.boot") version "2.2.6.RELEASE"
  id("io.spring.dependency-management") version "1.0.9.RELEASE"
  id("com.palantir.docker") version "0.25.0"
  kotlin("jvm") version "1.3.71"
  kotlin("plugin.spring") version "1.3.71"
}

group = "com.github.findcoo.spring-cloud-lab"
version = "1.3"
java.sourceCompatibility = JavaVersion.VERSION_1_8

extra["springCloudVersion"] = "Hoxton.SR4"

allprojects {
  repositories {
    mavenCentral()
    maven { url = URI("https://repo.spring.io/milestone") }
    maven { url = URI("https://maven.oracle.com") }
    maven { url = URI("https://jitpack.io") }
  }
}

subprojects {
  apply(plugin = "kotlin")
	apply(plugin = "kotlin-spring")
  apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")
  apply(plugin = "com.palantir.docker")

  dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
      exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
  }

  dependencyManagement {
    imports {
      mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
  }

  tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
  }

  tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      jvmTarget = "1.8"
    }
  }
}

tasks.getByName<BootJar>("bootJar") {
  enabled = false
}
