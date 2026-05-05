package com.traveling.travel_backend.service;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.FavoriteResponseDTO;
import com.traveling.travel_backend.exception.ResourceNotFoundException;
import com.traveling.travel_backend.exception.UnauthorizedException;
import com.traveling.travel_backend.model.Favorite;
import com.traveling.travel_backend.model.LogEntity;
import com.traveling.travel_backend.model.Place;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.FavoriteRepository;
import com.traveling.travel_backend.repository.LogRepository;
import com.traveling.travel_backend.repository.PlaceRepository;
import com.traveling.travel_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);
    private static final String LOG_MODULE = "FAVORITOS";

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final LogRepository logRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository,
                           PlaceRepository placeRepository, LogRepository logRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository     = userRepository;
        this.placeRepository    = placeRepository;
        this.logRepository      = logRepository;
    }

    @Transactional
    public FavoriteResponseDTO addFavorite(Authentication authentication, Long placeId) {
        Long userId = resolveUserId(authentication);

        logger.info("{} [{}] Solicitud de agregado -> Usuario: {}, Lugar: {}",
                AppConstants.PREFIX_FAVORITE, LOG_MODULE, userId, placeId);
        logRepository.save(new LogEntity(LOG_MODULE, AppConstants.LOG_INFO,
                "Solicitud de agregado -> Usuario: " + userId + ", Lugar: " + placeId +
                " - POST /api/favorites/user/" + userId + "/place/" + placeId, userId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("{} [{}] Usuario ID {} no encontrado", AppConstants.PREFIX_FAVORITE, LOG_MODULE, userId);
                    logRepository.save(new LogEntity(LOG_MODULE, AppConstants.LOG_WARN,
                            "Usuario ID " + userId + " no encontrado.", userId));
                    return new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
                });

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> {
                    logger.warn("{} [{}] Lugar ID {} no encontrado", AppConstants.PREFIX_FAVORITE, LOG_MODULE, placeId);
                    logRepository.save(new LogEntity(LOG_MODULE, AppConstants.LOG_WARN,
                            "Lugar ID " + placeId + " no encontrado.", userId));
                    return new ResourceNotFoundException("Lugar no encontrado con ID: " + placeId);
                });

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndPlaceId(userId, placeId);

        Favorite favorite;
        if (existing.isPresent()) {
            favorite = existing.get();
            favorite.setState(true);
        } else {
            favorite = new Favorite(user, place);
            favorite.setState(true);
        }

        Favorite saved = favoriteRepository.save(favorite);

        logger.info("{} [{}] Favorito guardado -> Usuario: {}, Lugar: {}",
                AppConstants.PREFIX_FAVORITE, LOG_MODULE, userId, placeId);
        logRepository.save(new LogEntity(LOG_MODULE, AppConstants.LOG_INFO,
                "Favorito guardado correctamente -> Usuario: " + userId + ", Lugar: " + placeId, userId));

        return FavoriteResponseDTO.fromEntity(saved);
    }

    @Transactional
    public List<FavoriteResponseDTO> getUserFavorites(Authentication authentication) {
        Long userId = resolveUserId(authentication);

        logger.info("{} [{}] Consultando favoritos del usuario ID: {}",
                AppConstants.PREFIX_FAVORITE, LOG_MODULE, userId);
        logRepository.save(new LogEntity(LOG_MODULE, AppConstants.LOG_INFO,
                "Consultando lista del usuario ID: " + userId +
                " - GET /api/favorites/user/" + userId, userId));

        List<Favorite> favorites = favoriteRepository.findByUserIdAndStateTrue(userId);

        if (favorites.isEmpty()) {
            logger.info("{} [{}] Usuario ID {} no tiene favoritos",
                    AppConstants.PREFIX_FAVORITE, LOG_MODULE, userId);
            logRepository.save(new LogEntity(LOG_MODULE, AppConstants.LOG_INFO,
                    "El usuario ID: " + userId + " no tiene favoritos.", userId));
        } else {
            logger.info("{} [{}] {} favoritos encontrados para usuario ID: {}",
                    AppConstants.PREFIX_FAVORITE, LOG_MODULE, favorites.size(), userId);
            logRepository.save(new LogEntity(LOG_MODULE, AppConstants.LOG_INFO,
                    "Devolviendo " + favorites.size() + " favoritos para usuario ID: " + userId, userId));
        }

        return favorites.stream()
                .map(FavoriteResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFavorite(Authentication authentication, Long placeId) {
        Long userId = resolveUserId(authentication);

        Favorite favorite = favoriteRepository.findByUserIdAndPlaceId(userId, placeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Favorito no encontrado para usuario: " + userId + " y lugar: " + placeId));

        favorite.setState(false);
        favoriteRepository.save(favorite);

        logger.info("{} [{}] Favorito desactivado -> Usuario: {}, Lugar: {}",
                AppConstants.PREFIX_FAVORITE, LOG_MODULE, userId, placeId);
        logRepository.save(new LogEntity(LOG_MODULE, AppConstants.LOG_INFO,
                "Favorito desactivado (soft delete) -> Usuario: " + userId + ", Lugar: " + placeId, userId));
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("No autenticado.");
        }
        return userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Usuario no válido."))
                .getId();
    }
}