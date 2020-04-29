import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI


plugins {
  id("org.springframework.boot") version "2.2.6.RELEASE"
  id("io.spring.dependency-management") version "1.0.9.RELEASE"
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
    maven { url = URI("https://jitpack.io") }
  }
}

subprojects {
  apply(plugin = "kotlin")
	apply(plugin = "kotlin-spring")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")

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

  tasks.withType<Test> {
    useJUnitPlatform()
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      jvmTarget = "1.8"
    }
  }

  tasks.bootJar {
    enabled = false
  }

  tasks.jar {
    enabled = true
  }

}


