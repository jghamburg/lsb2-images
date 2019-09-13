package com.greglturnquist.learningspringboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.reactor.core.ReactorCoreProperties;
import reactor.core.publisher.Hooks;

// tag::code[]
@SpringBootApplication
public class LearningSpringBootImagesApplication {

  public static void main(String[] args) {
    SpringApplication.run(
        LearningSpringBootImagesApplication.class, args);
  }

  @Autowired
  protected void initialize(ReactorCoreProperties properties) {
    if (properties.getStacktraceMode().isEnabled()) {
      Hooks.onOperatorDebug();
    }
  }
}
// end::code[]
