plugins {
	java
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	implementation("com.example:testing:0.0.1-SNAPSHOT")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
