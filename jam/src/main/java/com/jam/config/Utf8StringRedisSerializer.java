package com.jam.config;

import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.serializer.StringRedisSerializer;

public class Utf8StringRedisSerializer extends StringRedisSerializer  {

	public Utf8StringRedisSerializer() {
        super(StandardCharsets.UTF_8);
    }
}
