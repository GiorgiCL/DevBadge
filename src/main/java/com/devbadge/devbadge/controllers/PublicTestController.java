package com.devbadge.devbadge.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicTestController {

    @GetMapping("/api/public/test")
    public String test() {
        return "Public endpoint OK";
    }
}
