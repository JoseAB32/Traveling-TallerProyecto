package com.traveling.travel_backend.controller;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.repository.LogRepository;

import java.util.List;

import org.slf4j.Logger;           
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
    

@RestController
@RequestMapping("/api/admin/logs")
@CrossOrigin(origins = "http://localhost:4200")
public class LogController {

    public static final Logger log = LoggerFactory.getLogger(LogController.class);

    @Value("${app.features.admin-logs-enabled}")
    private boolean isAdminLogsEnabled;

    @Autowired
    private LogRepository logRepository;

    @GetMapping
    public List<LogEntity> getAll() {
        if (!isAdminLogsEnabled) {
            log.warn("🖥️ [ADMIN] Acceso denegado a logs generales");
            return List.of();
        }
        log.info("🖥️ [ADMIN] Solicitud de visualización de logs generales");
        return logRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    @GetMapping("/filter")
    public List<LogEntity> filterLogs(
        @RequestParam String module, 
        @RequestParam String level, 
        @RequestParam String startDate, 
        @RequestParam String endDate
    ) {
        if (!isAdminLogsEnabled) {
            log.warn("🖥️ [ADMIN] Acceso denegado a filtrado de logs");
            return List.of();
        }
        log.info("🖥️ [ADMIN] Solicitud de filtrado de logs - Módulo: {}, Nivel: {}, Fecha inicio: {}, Fecha fin: {}", module, level, startDate, endDate);
        return logRepository.findByModuleAndLevelAndTimestampBetweenOrderByTimestampDesc(
            module, 
            level, 
            java.time.LocalDateTime.parse(startDate), 
            java.time.LocalDateTime.parse(endDate)
        );
    }
    
}
