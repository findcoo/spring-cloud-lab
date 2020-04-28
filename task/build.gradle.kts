subprojects {
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-batch")
        implementation("org.springframework.cloud:spring-cloud-starter-task")

        testImplementation("org.springframework.batch:spring-batch-test")
    }
}