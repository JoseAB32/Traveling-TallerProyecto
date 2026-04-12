# 🗺️ Traveling App - Explorador de Lugares Turísticos y planificador de viajes

Una aplicación web interactiva diseñada para explorar los mejores destinos turísticos de Bolivia. Los usuarios pueden interactuar con mapas dinámicos, visualizar tarjetas de información destacadas con efectos visuales avanzados, leer las mejores reseñas aportadas por la comunidad y por supuesto crear sus propios itinerarios de viaje.

---

## 🚀 Tecnologías y Stack

**Frontend:**
* Angular CLI: `18.2.12` | Node.js: `22.11.0` | NPM: `11.11.0`
* Bootstrap 5 (UI/UX)
* Leaflet (Mapas Interactivos)

**Backend:**
* Java & Spring Boot: `4.0.3`
* Spring Data JPA
* Maven (Gestión de dependencias)
* MySQL (Base de datos relacional)

---

## 📁 Arquitectura del Frontend (Angular)

El proyecto cliente está diseñado con una arquitectura modular y escalable. Todo el código fuente reside dentro de `src/app/`, organizado en los siguientes directorios estratégicos:

* 🧩 **`components/`**: Componentes UI reutilizables y "tontos" (presentacionales) como el mapa (`<app-map>`), cabecera (`<app-header>`) y pie de página (`<app-footer>`).
* 📦 **`features/`**: Módulos o dominios de características específicas que pueden activarse o desactivarse. **Por ejemplo, aquí reside la lógica de la Vista de Administrador** y otras herramientas que no pertenecen al flujo público común.
* 🛡️ **`guards/`**: Guardianes de rutas (Auth Guards) para proteger el acceso a vistas que requieren autenticación o permisos especiales.
* 📡 **`interceptors/`**: Interceptores HTTP para inyectar tokens JWT en las peticiones, o manejar errores globales del backend.
* 📝 **`models/`**: Clases de TypeScript (ej. `Place`, `Review`, `User`) que mapean exactamente las respuestas del backend.
* 📄 **`pages/`**: Componentes enrutables o "inteligentes" (ej. `DepartmentComponent`) que consumen servicios y actúan como vistas completas de la aplicación.
* ⚙️ **`services/`**: Clases inyectables que manejan la lógica de negocio y las llamadas HTTP al backend (`PlaceService`, `ReviewService`).
* 🛠️ **`utils/`**: Funciones de ayuda general y el almacenamiento centralizado de **Constantes del Frontend** (URLs de la API, mensajes de error, configuraciones estáticas) para evitar "números mágicos" o strings repetidos en el código.

---

## 💡 Gestión de Constantes y Buenas Prácticas
Para mantener el código limpio y fácil de mantener, ambos entornos utilizan archivos centralizados de constantes:
* **En el Backend (`AppConstants.java`):** Se definen los prefijos de las rutas, nombres de las tablas para logs, estados de éxito/error y mensajes estáticos.
* **En el Frontend (`utils/`):** Se manejan las URLs base de los endpoints y configuraciones visuales compartidas, asegurando que si un dominio cambia, solo se modifique un archivo.

---

## ⚠️ Vista de Administrador (Entorno de Desarrollo)

Actualmente, el proyecto incluye un módulo (`feature`) de **Vista de Administrador** para facilitar la carga de datos, moderación de reseñas y pruebas rápidas durante la fase de construcción. 

**IMPORTANTE:** Esta vista y sus rutas asociadas están habilitadas **ÚNICAMENTE para el entorno de desarrollo**. Por motivos de seguridad, antes de realizar el proceso de *deploy* a producción, este feature será removido del build final o estrictamente bloqueado tras autenticación de alto nivel (ver sección de Feature Toggles).

---

## 🎛️ Feature Toggles (Control de Funcionalidades)

El sistema implementa una arquitectura de doble capa para el manejo de funcionalidades (*Feature Toggles*), separando la seguridad de la infraestructura de la lógica de negocio:

### 1. Toggles Estáticos de Infraestructura (Frontend)
Administrados mediante un archivo TypeScript compilable (`features.ts`). Se utilizan para bloquear el acceso a rutas y componentes sensibles antes de enviar a producción.
* **Ejemplo (`adminLogsEnabled`):** Una llave maestra que, al establecerse en `false`, elimina por completo el renderizado y acceso a la Vista de Administrador, garantizando que el código no esté expuesto en el build final.

### 2. Toggles Dinámicos de Negocio (Backend + JSON)
Administrados en tiempo real mediante un archivo `features.json` gestionado por Spring Boot (`/api/features`). Permite a los administradores encender o apagar lógicas de negocio en vivo desde el panel de control sin necesidad de reiniciar el servidor o recompilar Angular.
* **`pinRedirection`:** Habilita/deshabilita la redirección automática a las páginas de detalles cuando un usuario hace clic en los marcadores del mapa interactivo.
* **`autoCreateItinerary`:** Activa la funcionalidad de generación inteligente de viajes para los usuarios.

---

## 🔌 API Endpoints (Backend)

> **📌 Regla General de Identificadores (IDs):**
> La gran mayoría de los endpoints de esta API siguen una convención estándar RESTful para la manipulación de datos. Las consultas que requieren identificar un recurso específico siempre solicitan el `id`, ya sea a través de una variable de ruta (`/recurso/{id}`) o como un parámetro de consulta (`?recursoId={id}`), manteniendo la predictibilidad en el consumo desde el Frontend.

### Ejemplos de Endpoints:

* **Obtener Lugares por Departamento:** `GET /api/places/department/{id}`
  Retorna la lista de lugares turísticos asociados a un departamento.
* **Top 5 Mejor Calificados:** `GET /api/places/top-rated`
  Devuelve una lista con los 5 lugares con el promedio de calificación más alto.
* **Mejor Reseña de un Lugar:** `GET /api/places/mejor-resenia?placeId={id}`
  Busca y retorna únicamente la reseña con el puntaje más alto para un lugar, ignorando las que estén dadas de baja (`state = false`). Registra la acción en la BD mediante logs.
* **Gestión de Features:** `GET /api/features` y `PUT /api/features`
  Lee y actualiza el archivo JSON de configuración de los toggles dinámicos en el servidor.

---

## ⚙️ Instalación y Despliegue Local

### 1. Configuración de la Base de Datos
* Crea una base de datos en tu gestor MySQL (ej. `traveling_db`).
* Ejecuta el script SQL principal para generar las tablas y cargar los datos semilla.

### 2. Levantar el Backend (Spring Boot)
Abre una terminal, navega a la carpeta del backend y sigue estos pasos:
1. Configura tus credenciales en `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/traveling_db
   spring.datasource.username=TU_USUARIO
   spring.datasource.password=TU_CONTRASEÑA
   ```
2. Ejecuta el servidor (asegúrate de tener JDK 17+ instalado):
   ```bash
   mvn spring-boot:run
   ```

### 3. Levantar el Frontend (Angular)
Abre una **nueva terminal**, navega a la carpeta del frontend y ejecuta:
1. Instala todas las dependencias necesarias:
   ```bash
   npm install
   ```
2. Levanta el servidor de desarrollo local:
   ```bash
   ng serve
   ```
3. Abre tu navegador y dirígete a `http://localhost:4200`.

---

## 🤝 Contribución al Proyecto

1. Crea una rama desde `main` para tu nueva funcionalidad: `git checkout -b feature/nombre-funcionalidad`
2. Realiza tus commits con descripciones claras.
3. Abre un Pull Request documentando los cambios visuales y lógicos realizados.
4. La rama creada debe seguir el formato `US-10/NOMBREHU` ---> HU = historia de usuario

---

## 👥 Autores
* Vanessa Canavriri Zoto 
* Jose Maria Arias Balderrama 
* Mateo Sebastian Gandarillas Navia
* Marcelo David Navia Rocabado
```