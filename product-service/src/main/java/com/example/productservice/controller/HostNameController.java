package com.example.productservice.controller;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HostNameController {
    private Environment env;

    public HostNameController(Environment env) {
        this.env = env;
    }

    @GetMapping(path = "/hostname")
    public ResponseEntity<String> hostname() {
        String hostname = env.getProperty("HOSTNAME"); //(container ID)
        System.out.println("HOST NAME" + hostname);
        return ResponseEntity.ok().body(hostname);
    }
}
 