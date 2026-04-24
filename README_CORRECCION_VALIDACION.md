# Correccion y validacion de arranque local

Este documento resume lo que se tuvo que corregir para poder iniciar el proyecto localmente sin errores de configuracion y deja una guia corta para nuevos colaboradores.

## 1) Correccion aplicada en configuracion de entorno

### Problema observado

Al ejecutar backend en local, el sistema no siempre cargaba correctamente variables desde `.env` y fallaba con mensajes como:

- `Could not resolve placeholder 'JWT_SECRET'`

### Causa

La ruta de carga de dotenv quedaba sensible al directorio de ejecucion.

### Correccion aplicada

Se ajusto la propiedad de dotenv para que lea el `.env` del directorio del backend al ejecutar desde `travel-backend`.

Archivo afectado:

- `travel-backend/src/main/resources/application-dev.properties`
