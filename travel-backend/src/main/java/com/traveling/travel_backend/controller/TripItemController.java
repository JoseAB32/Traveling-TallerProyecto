package com.traveling.travel_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.repository.TripItemRepository;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH)
@CrossOrigin(origins = AppConstants.CORS_LOCALHOST)
public class TripItemController {

    @Autowired
    private TripItemRepository tripItemRepository;
}
