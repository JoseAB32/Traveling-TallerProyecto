interface ApiConstants {
  BASE_URL: string;
  CITIES: string;
  PLACES: string;
  USERS: string;
  LOGIN: string;
  LOGS: string;
  FAVORITES: string;
}

interface LandingConstants {
  SIGN_UP: string;
  LOGIN: string;
  TEXT_START_HERE: string;
  SUBTITLE_1: string;
  TEXT_1: string;
  SUBTITLE_2: string;
  TEXT_2: string;
}

interface MessagesConstants {
  SUCCESS: {
    LOAD_CITIES: string;
    LOGIN: string;
  };
  ERROR: {
    GENERAL: string;
    LOAD_CITIES: string;
    INVALID: string;
    LOGIN: string;
    SIGNUP_GENERAL: string;
    PASSWORD_MISMATCH: string;
    FILTER_REQUIRED: string;
    USERNAME_REQUIRED: string;
    USERNAME_MINLENGTH: string;
    USERNAME_PATTERN: string;
    EMAIL_REQUIRED: string;
    EMAIL_INVALID: string;
    PASSWORD_REQUIRED: string;
    PASSWORD_MINLENGTH: string;
    PASSWORD_PATTERN: string;
  };
}

interface LogsConstants {
  API_CALL: string;
  SUCCESS: string;
  ERROR: string;
}

export const CONSTANTS = {
  LANDING: {
    SIGN_UP: 'EMPIEZA AQUÍ',
    LOGIN: 'YA TENGO UNA CUENTA',
    TEXT_START_HERE: 'Planifica tu viaje y disfruta de los mejores lugares turísticos de toda Bolivia!',
    SUBTITLE_1: 'fácil, accesible y gratis',
    TEXT_1: 'Explora de forma simple y gratis las distintas atracciones turísticas que se encuentran en Bolivia para planificar un increíble y productivo viaje sin perderte maravillosos paisajes.',
    SUBTITLE_2: 'información actualizada y completa',
    TEXT_2: 'Explora de forma simple y gratis las distintas atracciones turísticas que se encuentran en Bolivia para planificar un increíble y productivo viaje sin perderte maravillosos paisajes.'
  } as LandingConstants,

  API: {
    BASE_URL: 'http://localhost:8080',
    CITIES: '/api/cities',
    PLACES: '/api/places',
    USERS: '/api/users',
    REVIEWS: '/api/reviews',
    LOGIN: '/api/login',
    LOGS: '/api/admin/logs',
    FAVORITES: '/api/favorites'
  } as ApiConstants,

  MESSAGES: {
    SUCCESS: {
      LOAD_CITIES: 'Ciudades cargadas correctamente',
      LOGIN: 'Login exitoso'
    },
    ERROR: {
      GENERAL: 'Ocurrió un error',
      LOAD_CITIES: 'Error al cargar ciudades',
      INVALID: 'Error',
      LOGIN: 'Error en el login',
      SIGNUP_GENERAL: 'No se pudo registrar el usuario. Revise datos e intente de nuevo.',
      PASSWORD_MISMATCH: 'Las contraseñas no coinciden.',
      FILTER_REQUIRED: 'Todos los campos de filtro son obligatorios para realizar la búsqueda precisa.',
      USERNAME_REQUIRED: 'El nombre de usuario es obligatorio.',
      USERNAME_MINLENGTH: 'El nombre de usuario debe tener al menos 5 caracteres.',
      USERNAME_PATTERN: 'El nombre de usuario no puede contener espacios.',
      EMAIL_REQUIRED: 'El correo electrónico es obligatorio.',
      EMAIL_INVALID: 'Ingresa un correo electrónico válido.',
      PASSWORD_REQUIRED: 'La contraseña es obligatoria.',
      PASSWORD_MINLENGTH: 'La contraseña debe tener al menos 8 caracteres.',
      PASSWORD_PATTERN: 'La contraseña no puede contener espacios.'
    }
  } as MessagesConstants,

  LOGS: {
    API_CALL: 'Llamando a API:',
    SUCCESS: 'Operación exitosa',
    ERROR: 'Error detectado'
  } as LogsConstants

};