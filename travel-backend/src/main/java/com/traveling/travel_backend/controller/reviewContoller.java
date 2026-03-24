package com.traveling.travel_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveling.travel_backend.repository.ReviewRepository;

@RestController
@RequestMapping("api/")
@CrossOrigin(origins = "http://localhost:4200")
public class reviewContoller {
    
    @Autowired
    private ReviewRepository reviewRepository;
}
