plugins {
	java
	id("org.springframework.boot") version "3.0.2" // TODO: remove
	id("io.spring.dependency-management") version "1.1.0" // TODO: remove
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

dependencies {
	implementation("com.example:testing:0.0.1-SNAPSHOT")
	implementation("org.junit.jupiter:junit-jupiter:5.9.2")
	implementation("org.springframework.data:spring-data-jdbc:3.0.1") { // TODO: remove
		description = "for RowMapper"
	}
}

dependencyManagement {

}

tasks.withType<Test> {
	useJUnitPlatform()
}
