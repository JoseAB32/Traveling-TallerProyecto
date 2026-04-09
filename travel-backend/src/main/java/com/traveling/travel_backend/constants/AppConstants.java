package com.traveling.travel_backend.constants;

/**
 * Constantes globales de la aplicación Travel
 * Centraliza todos los valores constantes para evitar strings mágicos
 */
public class AppConstants {

    // Prevenir instanciación
    private AppConstants() {
        throw new AssertionError("No se puede instanciar AppConstants");
    }

    // ==================== API ROUTES ====================
    public static final String API_BASE_PATH = "/api";
    public static final String USERS_ENDPOINT = "/users";
    public static final String CITIES_ENDPOINT = "/cities";
    public static final String PLACES_ENDPOINT = "/places";
    public static final String PLACES_DEPARTMENT = "/department/{cityId}";
    public static final String PLACES_DEPARTMENT_TOP = "/departmentTop/{cityId}";
    public static final String REVIEWS_ENDPOINT = "/reviews";
    public static final String FAVORITES_ENDPOINT = "/favorites";
    public static final String TRIPS_ENDPOINT = "/trips";
    public static final String TRIP_ITEMS_ENDPOINT = "/tripitems";
    public static final String LOGS_ENDPOINT = "/logs";
    public static final String TOP_RATED = "/top-rated";
    public static final String SEARCH = "/search";
    public static final String LOGIN = "/login";

    // ==================== CORS ====================
    public static final String CORS_LOCALHOST = "http://localhost:4200";
    public static final String CORS_ALL = "*";

    // ==================== HTTP STATUS CODES ====================
    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    // ==================== USER MESSAGES ====================
    public static final String USER_REQUIRED = "El nombre de usuario es requerido.";
    public static final String USER_ALREADY_IN_USE = "El nombre de usuario ya está en uso.";
    public static final String USER_CREATED_SUCCESS = "Usuario creado exitosamente";
    public static final String USER_UPDATED_SUCCESS = "Usuario actualizado exitosamente";
    public static final String USER_DELETED_SUCCESS = "Usuario eliminado exitosamente";
    public static final String USER_NOT_FOUND = "Usuario no encontrado";
    public static final String INVALID_CREDENTIALS = "Credenciales inválidas";
    public static final String EMAIL_ALREADY_REGISTERED = "El correo ya está registrado";

    // ==================== PLACE MESSAGES ====================
    public static final String PLACE_NOT_FOUND = "Lugar no encontrado";
    public static final String PLACE_CREATED_SUCCESS = "Lugar creado exitosamente";
    public static final String PLACE_UPDATED_SUCCESS = "Lugar actualizado exitosamente";
    public static final String PLACE_DELETED_SUCCESS = "Lugar eliminado exitosamente";

    // ==================== CITY MESSAGES ====================
    public static final String CITIES_FOUND = "Ciudades encontradas: {} y devueltas correctamente.";
    public static final String NUM_CITIES_FOUND = "Número de ciudades encontradas: {}";

    // ==================== LOG CATEGORIES ====================
    public static final String LOG_USERS = "USERS";
    public static final String LOG_PLACES = "PLACES";
    public static final String LOG_CITIES = "CIUDADES";
    public static final String LOG_REVIEWS = "REVIEWS";
    public static final String LOG_FAVORITES = "FAVORITES";
    public static final String LOG_TRIPS = "TRIPS";

    // ==================== LOG LEVELS ====================
    public static final String LOG_INFO = "INFO";
    public static final String LOG_DEBUG = "DEBUG";
    public static final String LOG_WARN = "WARN";
    public static final String LOG_ERROR = "ERROR";

    // ==================== LOG PREFIXES ====================
    public static final String PREFIX_USER = "👤";
    public static final String PREFIX_PLACE = "📍";
    public static final String PREFIX_CITY = "🏙️";
    public static final String PREFIX_ERROR = "❌";

    // ==================== VALIDATION ====================
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final double MIN_RATING = 0.0;
    public static final double MAX_RATING = 5.0;

    // ==================== PAGINATION ====================
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int TOP_RATED_LIMIT = 5;
    public static final int TOP_PLACES_BY_DEPARTMENT_LIMIT = 10;

    // ==================== CATEGORIES ====================
    public static final String CATEGORY_BEACH = "PLAYA";
    public static final String CATEGORY_MOUNTAIN = "MONTAÑA";
    public static final String CATEGORY_CITY = "CIUDAD";
    public static final String CATEGORY_PARK = "PARQUE";
    public static final String CATEGORY_MUSEUM = "MUSEO";
    public static final String CATEGORY_HISTORICAL = "HISTÓRICO";
    public static final String CATEGORY_ADVENTURE = "AVENTURA";
    public static final String CATEGORY_RESTAURANT = "RESTAURANTE";

    // ==================== RESPONSE FIELDS ====================
    public static final String SUCCESS_KEY = "success";
    public static final String MESSAGE_KEY = "message";
    public static final String DATA_KEY = "data";
    public static final String ERROR_KEY = "error";

    // ==================== ERROR CODES ====================
    public static final String ERROR_CODE_VALIDATION = "ERR_VALIDATION";
    public static final String ERROR_CODE_NOT_FOUND = "ERR_NOT_FOUND";
    public static final String ERROR_CODE_UNAUTHORIZED = "ERR_UNAUTHORIZED";
    public static final String ERROR_CODE_CONFLICT = "ERR_CONFLICT";

}
