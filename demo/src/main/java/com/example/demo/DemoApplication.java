package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;

@Configuration
class Config {
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}

@SpringBootApplication
@RestController
@Configuration
@RequiredArgsConstructor
public class DemoApplication {

	private static final List<String> GREETINGS = List.of("Hello", "Welcome", "Cheers");
	private final JdbcTemplate jdbcTemplate;
	private final RestTemplate restTemplate;
	@Value("${app.greetings-url}")
	private String greetingsUrl;

	@GetMapping("/greeting/{userId}")
	@ResponseBody
	public String greetingUserById(@PathVariable Integer userId) {
		var userName = jdbcTemplate.queryForObject(
				"select name from auto_test_user where id = " + userId,
				String.class
		);

		var index = new Random().nextInt(GREETINGS.size());
		var greetingsResp = restTemplate.getForEntity(greetingsUrl + "/greetings/" + index, String.class);
		var greeting = greetingsResp.getBody();
		return greeting + ", " + userName + "!";
	}

	@GetMapping("/greetings/{index}")
	public String greetings(@PathVariable Integer index) {
		return GREETINGS.get(index);
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
