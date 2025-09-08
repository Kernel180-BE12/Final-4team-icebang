package com.gltkorea.icebang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("unit")
@ActiveProfiles("test-unit")
public @interface UnitTest {}
