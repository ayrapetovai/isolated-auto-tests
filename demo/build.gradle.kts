plugins {
	java
	id("org.springframework.boot") version "3.0.2"
	id("io.spring.dependency-management") version "1.1.0"
	id("com.palantir.docker") version "0.34.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.liquibase:liquibase-core")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks {
	named<Test>("test") {
		useJUnitPlatform()
	}
	named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
		archiveFileName.set("app.jar")
	}
}

tasks.register<Copy>("unpack") {
	dependsOn(tasks.bootJar)
	from(tasks["bootJar"].outputs.files.singleFile)
	into("build/dependency")
}

docker {
	name = "${project.group}/${rootProject.name}"
	copySpec.from(tasks["unpack"].outputs).into("dependency")
	buildArgs(mapOf("DEPENDENCY" to "dependency"))
}

