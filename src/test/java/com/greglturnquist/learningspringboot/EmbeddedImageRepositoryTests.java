/*
 * Copyright 2017 the original author or authors.
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
package com.greglturnquist.learningspringboot;

import static org.assertj.core.api.Assertions.assertThat;

import com.greglturnquist.learningspringboot.images.Image;
import com.greglturnquist.learningspringboot.images.ImageRepository;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Greg Turnquist
 */
// tag::1[]
//@ContextConfiguration(classes = LearningSpringBootImagesApplication.class)
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class EmbeddedImageRepositoryTests {

	@Autowired
	ImageRepository repository;

	// end::1[]
	@Autowired
	MongoOperations operations;
	// tag::2[]
	/**
	 * To avoid {@code block()} calls, use blocking {@link MongoOperations} during setup.
	 */
	//@Before
	public void setUp() {
		operations.dropCollection(Image.class);

		operations.insert(new Image("1",
			"learning-spring-boot-cover.jpg", "greg"));
		operations.insert(new Image("2",
			"learning-spring-boot-2nd-edition-cover.jpg", "greg"));
		operations.insert(new Image("3",
			"bazinga.png", "greg"));

		operations.findAll(Image.class).forEach(image -> {
			System.out.println(image.toString());
		});
	}
	// end::2[]

	// tag::3[]
	@Test
	public void findAllShouldWork() {
		List<Image> images = repository.findAll().collectList().block();

		assertThat(images).hasSize(3);
		assertThat(images)
			.extracting(Image::getName)
			.contains(
				"learning-spring-boot-cover.jpg",
				"learning-spring-boot-2nd-edition-cover.jpg",
				"bazinga.png");
	}
	// end::3[]

	// tag::4[]
	@Test
	public void findByNameShouldWork() {
		Image image = repository.findByName("bazinga.png").block();

		assertThat(image.getName()).isEqualTo("bazinga.png");
		assertThat(image.getId()).isEqualTo("3");
	}
	// end::4[]

}