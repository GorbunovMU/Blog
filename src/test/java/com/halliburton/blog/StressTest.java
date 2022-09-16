package com.halliburton.blog;

import org.junit.jupiter.api.Test;
import org.loadtest4j.LoadTester;
import org.loadtest4j.Request;
import org.loadtest4j.Result;
import org.loadtest4j.factory.LoadTesterFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = BlogApplication.class)
public class StressTest {

	private static final LoadTester loadTester = LoadTesterFactory.getLoadTester();

	@Test
	public void testGetUrlForBlogs() {
		List<Request> requests = List.of(Request.get("/api/v1.0.0/blogs/")
				.withHeader("Accept", "application/json")
				.withQueryParam("status", "available"));

		Result result = loadTester.run(requests);

		System.err.println("The median response time: " + result.getResponseTime().getMedian());
		System.err.println("The maximum response time: " + result.getResponseTime().getMax());
		System.err.println("The percent of OK requests: " + result.getPercentOk());
		System.err.println("RequestCount OK: " + result.getDiagnostics().getRequestCount().getOk());
		System.err.println("RequestCount Total: " + result.getDiagnostics().getRequestCount().getTotal());
		System.err.println("Requests per second: " + result.getDiagnostics().getRequestsPerSecond());

		assertThat(result.getResponseTime().getPercentile(90))
				.isLessThanOrEqualTo(Duration.ofMillis(15000));
	}

	@Test
	public void testGetUrlForPosts() {
		List<Request> requests = List.of(Request.get("/api/v1.0.0/posts/")
				.withHeader("Accept", "application/json")
				.withQueryParam("status", "available"));

		Result result = loadTester.run(requests);
		printResults(result);
		assertThat(result.getResponseTime().getPercentile(90))
				.isLessThanOrEqualTo(Duration.ofMillis(15000));
	}

	private void printResults(Result result) {
		System.err.println("The median response time: " + result.getResponseTime().getMedian());
		System.err.println("The maximum response time: " + result.getResponseTime().getMax());
		System.err.println("The percent of OK requests: " + result.getPercentOk());
		System.err.println("RequestCount OK: " + result.getDiagnostics().getRequestCount().getOk());
		System.err.println("RequestCount Total: " + result.getDiagnostics().getRequestCount().getTotal());
		System.err.println("Requests per second: " + result.getDiagnostics().getRequestsPerSecond());
	}
}