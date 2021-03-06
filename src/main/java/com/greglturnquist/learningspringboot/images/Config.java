/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.greglturnquist.learningspringboot.images;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Greg Turnquist
 */
// tag::code[]
@Configuration
public class Config {
/*
	@Bean
	@LoadBalanced
	WebClient webClient() {
		return WebClientBuilder.build();
	}
 */
// https://spring.io/blog/2018/06/20/the-road-to-reactive-spring-cloud
@Bean
WebClient webClient() {
  return WebClient.builder().build();
}
}
// end::code[]
