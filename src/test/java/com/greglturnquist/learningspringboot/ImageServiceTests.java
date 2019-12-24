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

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

import com.greglturnquist.learningspringboot.images.Image;
import com.greglturnquist.learningspringboot.images.ImageRepository;
import com.greglturnquist.learningspringboot.images.ImageService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Greg Turnquist
 */
// tag::1[]
@TestPropertySource(properties =
    {"spring.autoconfigure.exclude=com.gregturnquist.learningspringboot.webdriver.WebDriverAutoConfiguration"})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration
public class ImageServiceTests {

  @Autowired
  ImageService imageService;

  @MockBean
  ImageRepository repository;
  // end::1[]

  @MockBean(name = "setUp")
  Object ignoreImageServiceCommandLineRunner;

  @BeforeEach
  public void setUp() throws IOException {
    FileSystemUtils.deleteRecursively(new File(ImageService.UPLOAD_ROOT));
    Files.createDirectory(Paths.get(ImageService.UPLOAD_ROOT));
  }

  @Test
  public void findAllShouldJustReturnTheFlux() {
    // given
    given(repository.findAll()).willReturn(
        Flux.just(
            new Image("1", "alpha.jpg", "greg"),
            new Image("2", "bravo.jpg", "phil")
        )
    );

    // when
    Flux<Image> images = imageService.findAllImages();

    // then
    then(images).isNotNull();
    then(images.collectList().block())
        .hasSize(2)
        .extracting(Image::getId, Image::getName, Image::getOwner)
        .contains(
            tuple("1", "alpha.jpg", "greg"),
            tuple("2", "bravo.jpg", "phil")
        );
  }

  @Test
  public void findOneShouldReturnNotYetFetchedUrl() {
    // when
    Mono<Resource> image = imageService.findOneImage("alpha.jpg");

    // then
    then(image).isNotNull();
    Resource resource = image.block();
    then(resource.getDescription()).isEqualTo("URL [file:upload-dir/alpha.jpg]");
    then(resource.exists()).isFalse();
    then(resource.getClass()).isEqualTo(FileUrlResource.class);
  }

  @Test
  public void createImageShouldWork() {
    // given
    Image alphaImage = new Image("1", "alpha.jpg", "greg");
    Image bravoImage = new Image("2", "bravo.jpg", "phil");
    given(repository.save(new Image(any(), alphaImage.getName(), alphaImage.getOwner())))
        .willReturn(Mono.just(alphaImage));
    given(repository.save(new Image(any(), bravoImage.getName(), bravoImage.getOwner())))
        .willReturn(Mono.just(bravoImage));
    given(repository.findAll()).willReturn(Flux.just(alphaImage, bravoImage));
    FilePart file1 = mock(FilePart.class);
    given(file1.filename()).willReturn(alphaImage.getName());
    given(file1.transferTo(any(File.class))).willReturn(Mono.empty());
    FilePart file2 = mock(FilePart.class);
    given(file2.filename()).willReturn(bravoImage.getName());
    given(file2.transferTo(any(File.class))).willReturn(Mono.empty());
    Principal auth = mock(Principal.class);

    // when
    Mono<Void> done = imageService.createImage(Flux.just(file1, file2), auth);

    // then
    then(done.block()).isNull();
  }

  @Test
  @WithMockUser(username = "admin", roles = {"ADMIN"})
  public void deleteImageWithAdminShouldWork() {
    // given
    String imageName = "alpha.jpg";
    Mono<Image> image = Mono.just(new Image("1", imageName, "greg"));
    given(repository.findByName(any())).willReturn(image);
    given(repository.delete((Image) any())).willReturn(Mono.empty());

    // when
    Mono<Void> done = imageService.deleteImage(imageName);

    // then
    then(done.block()).isNull();
  }

  @Test
  public void deleteImageWithUnauthorizedUserShouldNotWork() {
    // given
    String imageName = "beta.jpg";
    Mono<Image> image = Mono.just(new Image("1", imageName, "greg"));
    given(repository.findByName(any())).willReturn(image);
    given(repository.delete((Image) any())).willReturn(Mono.empty());

    // when
    Assertions.assertThrows(IllegalStateException.class, () -> {
      Mono<Void> done = imageService.deleteImage(imageName);
      then(done.block()).isNull();
    });
  }

}
