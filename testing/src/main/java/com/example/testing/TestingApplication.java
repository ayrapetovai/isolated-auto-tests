package com.example.testing;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.example.testing.Util.toJson;

@Slf4j
@SpringBootApplication
@RestController
@Configuration
@RequiredArgsConstructor
public class TestingApplication {

	private final RestMock restMock;

	@GetMapping("/**")
	public Object interceptor(HttpServletRequest request) throws IOException {
		record Header(String name, String value) {}
		var headerMap = StreamSupport.stream(
						Spliterators.spliteratorUnknownSize(
								request.getHeaderNames().asIterator(),
								Spliterator.ORDERED),
						false
				).map(headerName -> new Header(headerName, request.getHeader(headerName)))
				.collect(Collectors.toMap(Header::name, Header::value));

		var bodyReader = request.getReader();
		var body = new StringBuilder();
		String bodyLine;
		while (true) {
			bodyLine = bodyReader.readLine();
			if (bodyLine != null) {
				body.append(bodyLine);
			} else {
				break;
			}
		}
		bodyReader.close();

		var requestData = new RequestData(
				request.getMethod(),
				request.getRequestURI(),
				new HashMap<>(request.getParameterMap()),
				headerMap,
				body.toString()
		);

		log.info("inbound request: >>> {}", requestData);
		var response = restMock.handle(
				request.getRequestURI(),
				requestData
		);

		log.info("inbound request: <<< {}", toJson(response));
		return response;
	}

	public static void main(String[] args) {
		SpringApplication.run(TestingApplication.class, args);
	}

}
