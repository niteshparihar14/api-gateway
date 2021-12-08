package com.example.demo;

import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

@SpringBootApplication
@RestController
public class ApiGatewayApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@GetMapping(path = "/default")
	public String message() {
		return "Default Message";
	}
	
	@Bean
	@LoadBalanced
	public WebClient.Builder builderRef(){
		
		return WebClient.builder();
	}
	
	@Bean
	@Primary
	public WebClient client(WebClient.Builder builderRef) {
		
		return builderRef.build();
	}
	
	@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder  routeBuilder) {
		
		return routeBuilder.routes().
				route(p->p
						.path("/api/v1/payments")
						.filters(f->f.circuitBreaker(c->c.setName("custom").setFallbackUri("/default") ))
						.uri("http://localhost:3050"))
						.build();
	}
	
	@Bean
	public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer(){
		
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
				.timeLimiterConfig(TimeLimiterConfig.custom()
						.timeoutDuration(Duration.ofSeconds(20)).build()).build());
	}
}
