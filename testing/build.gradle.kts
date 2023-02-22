plugins {
	java
	`maven-publish`
	`java-library`
	id("org.springframework.boot") version "3.0.2"
	id("io.spring.dependency-management") version "1.1.0"
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
	mavenLocal()
	mavenCentral()
}

extra["testcontainersVersion"] = "1.17.6"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
	testCompileOnly("org.projectlombok:lombok")
	implementation("org.postgresql:postgresql")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
	implementation("org.springframework.boot:spring-boot-starter-test")
	implementation("org.testcontainers:junit-jupiter")
	implementation("org.testcontainers:postgresql")
}

configurations {
	apiElements {
		outgoing {
			capability("org.springframework.data:spring-data-jdbc:3.0.1")
			capability("org.junit.jupiter:junit-jupiter:5.9.2")
		}
	}
}

dependencyManagement {
	imports {
		mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
	enabled = true
}

publishing {
	tasks.publish.get().dependsOn("build")
	repositories {
		mavenLocal()
	}
	publications {
		create<MavenPublication>("install") {
			artifactId = rootProject.name
			from(components["java"])

			versionMapping {
				usage("java-api") {
					fromResolutionOf("runtimeClasspath")
				}
				usage("java-runtime") {
					fromResolutionResult()
				}
			}
		}
	}
}