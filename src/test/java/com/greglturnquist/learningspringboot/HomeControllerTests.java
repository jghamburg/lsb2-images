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
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.verifyNoMoreInteractions;

import com.greglturnquist.learningspringboot.images.Image;
import com.greglturnquist.learningspringboot.images.ImageService;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Greg Turnquist
 */
// tag::1[]
@SpringBootTest
@Import({ThymeleafAutoConfiguration.class})
public class HomeControllerTests {

  @Autowired
  private WebTestClient webClient;

  @MockBean
  private ImageService imageService;
  // end::1[]

  // tag::2[]
  @Test
  public void baseRouteShouldListAllImages() {
    // given
    Image alphaImage = new Image("1", "alpha.png", "greg");
    Image bravoImage = new Image("2", "bravo.png", "phil");
    given(this.imageService.findAllImages())
        .willReturn(Flux.just(alphaImage, bravoImage));

    // when
    EntityExchangeResult<String> result = this.webClient
        .get().uri("/")
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class).returnResult();

    // then
    verify(this.imageService).findAllImages();
    verifyNoMoreInteractions(this.imageService);
    assertThat(result.getResponseBody())
        .contains(
            "<title>Learning Spring Boot: Spring-a-Gram</title>")
        .contains("<a href=\"/images/alpha.png/raw\">")
        .contains("<a href=\"/images/bravo.png/raw\">");
  }
  // end::2[]

  // tag::3[]
  @Test
  public void fetchingImageShouldWork() {
    given(imageService.findOneImage(any()))
        .willReturn(Mono.just(
            new ByteArrayResource("data".getBytes())));

    webClient
        .get().uri("/images/alpha.png/raw")
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class).isEqualTo("data");

    verify(imageService).findOneImage("alpha.png");
    verifyNoMoreInteractions(imageService);
  }
  // end::3[]

  // tag::4[]
  @Test
  public void fetchingNullImageShouldFail() throws IOException {
    Resource resource = mock(Resource.class);
    given(resource.getInputStream())
        .willThrow(new IOException("Bad file"));
    given(imageService.findOneImage(any()))
        .willReturn(Mono.just(resource));

    webClient
        .get().uri("/images/alpha.png/raw")
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody(String.class)
        .isEqualTo("Couldn't find alpha.png => Bad file");

    verify(imageService).findOneImage("alpha.png");
    verifyNoMoreInteractions(imageService);
  }
  // end::4[]

  // tag::5[]
  @Test
  @WithMockUser(username = "admin", roles = {"ADMIN"})
  public void deleteImageShouldWork() {
    given(imageService.deleteImage(any())).willReturn(Mono.empty());

    webClient
        .delete().uri("/images/alpha.png")
        .exchange()
        .expectStatus().isSeeOther()
        .expectHeader().valueEquals(HttpHeaders.LOCATION, "/");

    verify(imageService).deleteImage("alpha.png");
    verifyNoMoreInteractions(imageService);
  }
  // end::5[]

}
