package com.traveling.travel_backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveling.travel_backend.constants.AppConstants;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH)
@CrossOrigin(origins = {AppConstants.CORS_LOCALHOST, AppConstants.CORS_NETLIFY})
public class TripItemController {
}
