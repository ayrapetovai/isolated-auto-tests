package com.example.testing;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.example.testing.Util.toJson;

@Slf4j
@SpringBootApplication
@RestController
@RequiredArgsConstructor
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
public class TestingApplication {

	private final RestMock restMock;

	@RequestMapping("/**")
	public Object interceptor(HttpServletRequest request) {
		record Header(String name, String value) {}
		var headerMap = StreamSupport.stream(
						Spliterators.spliteratorUnknownSize(
								request.getHeaderNames().asIterator(),
								Spliterator.ORDERED),
						false
				).map(headerName -> new Header(headerName, request.getHeader(headerName)))
				.collect(Collectors.toMap(Header::name, Header::value));

		var body = new StringBuilder();
		var bodyReadError = "";
		try(var bodyReader = request.getReader()) {
			String bodyLine;
			while (true) {
				bodyLine = bodyReader.readLine();
				if (bodyLine != null) {
					body.append(bodyLine);
				} else {
					break;
				}
			}
		} catch (Exception e) {
			log.error("failed to read the body of the request", e);
			bodyReadError = e.getMessage();
		}

		var requestData = new RequestData(
				request.getMethod(),
				request.getRequestURI(),
				new HashMap<>(request.getParameterMap()),
				headerMap,
				body.toString(),
				bodyReadError
		);

		log.info("inbound request: >>> {}", requestData);
		var response = restMock.handle(request.getRequestURI(), requestData);

		log.info("inbound request: <<< {}", toJson(response));
		return response;
	}

	public static void main(String[] args) {
		SpringApplication.run(TestingApplication.class, args);
	}

}
