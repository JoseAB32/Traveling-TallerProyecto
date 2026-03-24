package com.traveling.travel_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveling.travel_backend.repository.FavoriteRepository;

@RestController
@RequestMapping("api/")
@CrossOrigin(origins = "http://localhost:4200")
public class favoriteController {

    @Autowired
    private FavoriteRepository favoriteRepository;
}
