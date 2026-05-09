package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/admin/logs")
@CrossOrigin(origins = { AppConstants.CORS_LOCALHOST, AppConstants.CORS_NETLIFY })
@Tag(name = "Log", description = "Administracion de logs del sistema")
public class LogController {

    private final LogService logService;

    @Value("${app.features.admin-logs-enabled}")
    private boolean isAdminLogsEnabled;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @Operation(summary = "Get all logs", description = "Returns a list of all logs", operationId = "getAllLogs")
    @GetMapping
    public ResponseEntity<List<LogEntity>> getAll() {
        return ResponseEntity.ok(logService.getAllLogs(isAdminLogsEnabled));
    }

    @Operation(summary = "Get filtered logs", description = "Returns logs filtered by module, level, start and end date", operationId = "filterLogs")
    @GetMapping("/filter")
    public ResponseEntity<List<LogEntity>> filterLogs(
            @RequestParam String module,
            @RequestParam String level,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(logService.filterLogs(isAdminLogsEnabled, module, level, startDate, endDate));
    }
}