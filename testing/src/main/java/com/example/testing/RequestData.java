package com.example.testing;

import java.util.Map;

public record RequestData(
    String method,
    String uri,
    Map<String, String[]> parameters,
    Map<String, String> headers,
    String body
){}
