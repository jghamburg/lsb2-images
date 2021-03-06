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

import com.greglturnquist.learningspringboot.ImagesConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/** @author Greg Turnquist */
@Component
public class CommentHelper {

  private final WebClient webClient;
  private final ImagesConfiguration imagesConfiguration;

  CommentHelper(WebClient webClient, ImagesConfiguration imagesConfiguration
      , @Value("${images.commentsUrl}") String commentsUrl) {
    this.webClient = webClient.mutate().baseUrl(commentsUrl).build();
    this.imagesConfiguration = imagesConfiguration;
  }

  public Flux<Comment> getComments(Image image, String sessionId) {

    return webClient
        .get()
        .uri("/comments/{id}", image.getId())
        .headers(
            h -> {
              String credentials =
                  imagesConfiguration.getCommentsUser()
                      + ":"
                      + imagesConfiguration.getCommentsPassword();
              String token = new String(Base64Utils.encode(credentials.getBytes()));
              h.add(HttpHeaders.AUTHORIZATION, "Basic " + token);
              h.add("Cookie", "SESSION=" + sessionId);
            })
        .retrieve()
        .bodyToFlux(Comment.class);
  }

}
