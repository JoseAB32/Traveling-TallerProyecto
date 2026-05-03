package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Transactional
    public List<LogEntity> getAllLogs(boolean isAdminLogsEnabled) {
        if (!isAdminLogsEnabled) {
            logger.warn("{} [ADMIN] Acceso denegado a logs generales", AppConstants.PREFIX_ADMIN);
            return List.of();
        }
        logger.info("{} [ADMIN] Solicitud de visualizacion de logs generales", AppConstants.PREFIX_ADMIN);
        return logRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    @Transactional
    public List<LogEntity> filterLogs(boolean isAdminLogsEnabled, String module, String level,
                                      String startDate, String endDate) {
        if (!isAdminLogsEnabled) {
            logger.warn("{} [ADMIN] Acceso denegado a filtrado de logs", AppConstants.PREFIX_ADMIN);
            return List.of();
        }
        logger.info("{} [ADMIN] Filtrado de logs - Modulo: {}, Nivel: {}, Desde: {}, Hasta: {}",
                AppConstants.PREFIX_ADMIN, module, level, startDate, endDate);

        return logRepository.findByModuleAndLevelAndTimestampBetweenOrderByTimestampDesc(
                module,
                level,
                LocalDateTime.parse(startDate),
                LocalDateTime.parse(endDate)
        );
    }
}