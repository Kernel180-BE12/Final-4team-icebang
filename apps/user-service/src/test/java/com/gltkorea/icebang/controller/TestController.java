package com.gltkorea.icebang.controller;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@TestComponent
@RestController
public class TestController {

  @GetMapping("/api/health")
  public String health() {
    return "OK";
  }
}
