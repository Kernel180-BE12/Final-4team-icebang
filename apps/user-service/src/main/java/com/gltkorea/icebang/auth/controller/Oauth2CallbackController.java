package com.gltkorea.icebang.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v0/oauth/callback")
@RequiredArgsConstructor
public class Oauth2CallbackController {}
