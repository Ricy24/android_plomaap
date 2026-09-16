# Informe de exposición - PlomApp Android Native

## 1. Datos generales

**Proyecto:** PlomApp  
**Tipo de app:** Android Native con Kotlin + Jetpack Compose  
**Backend:** Flask API REST  
**Ruta del proyecto Android:** [android_native](android_native)  
**Ruta del backend:** C:\Users\Anrid\plomaap_react_estable\backend_flask  
**Puerto del backend:** http://127.0.0.1:5000 o http://<IP-del-PC>:5000

## 2. Objetivo del proyecto

PlomApp es una aplicación móvil nativa para Android diseñada para conectar clientes con técnicos de plomería, además de ofrecer un flujo de autenticación, gestión de servicios, citas, perfil del usuario y administración de datos del hogar. La app nativa maneja la capa de presentación, navegación, validación de formularios y consumo de la API del backend.

## 3. Arquitectura general

La solución está dividida en dos capas:

- Cliente Android nativo: desarrollado con Kotlin, Jetpack Compose, Navigation, ViewModel, Retrofit y DataStore.
- Backend Flask: expone endpoints REST, autentica usuarios con JWT y gestiona la lógica de negocio, roles y persistencia.

La arquitectura de la app Android se puede observar en:

- [android_native/app/src/main/java/com/example/plomaap/data/api/ApiClient.kt](android_native/app/src/main/java/com/example/plomaap/data/api/ApiClient.kt)
- [android_native/app/src/main/java/com/example/plomaap/data/api/ApiService.kt](android_native/app/src/main/java/com/example/plomaap/data/api/ApiService.kt)
- [android_native/app/src/main/java/com/example/plomaap/data/repository/AuthRepository.kt](android_native/app/src/main/java/com/example/plomaap/data/repository/AuthRepository.kt)
- [android_native/app/src/main/java/com/example/plomaap/viewmodel/AuthViewModel.kt](android_native/app/src/main/java/com/example/plomaap/viewmodel/AuthViewModel.kt)
- [android_native/app/src/main/java/com/example/plomaap/ui/navigation/AppNavigation.kt](android_native/app/src/main/java/com/example/plomaap/ui/navigation/AppNavigation.kt)

## 4. Tecnologías implementadas

### Cliente Android Native
- Kotlin
- Jetpack Compose
- ViewModel
- StateFlow
- Navigation Compose
- Retrofit + OkHttp
- Gson
- DataStore Preferences
- Credential Manager para Google Sign-In y Passkeys

### Backend
- Flask
- Flask JWT Extended
- SQLAlchemy
- REST API modular
- Roles y permisos
- Endpoints para auth, servicios, técnicos, citas, administración y hogar digital

## 5. Evidencias del funcionamiento en Android

### 5.1 Login y autenticación
La pantalla de login está en [android_native/app/src/main/java/com/example/plomaap/ui/screens/auth/LoginScreen.kt](android_native/app/src/main/java/com/example/plomaap/ui/screens/auth/LoginScreen.kt). En ella se implementan:

- inicio de sesión con correo y contraseña
- Google Sign-In
- Passkey / biometría
- validación de email y contraseña
- manejo de errores y mensajes visibles al usuario

La lógica de negocio se encuentra en [android_native/app/src/main/java/com/example/plomaap/viewmodel/AuthViewModel.kt](android_native/app/src/main/java/com/example/plomaap/viewmodel/AuthViewModel.kt), donde se procesa el estado de la sesión y se consume el repositorio de autenticación.

### 5.2 Registro de usuario
El registro se realiza a través de la pantalla: [android_native/app/src/main/java/com/example/plomaap/ui/screens/auth/RegisterScreen.kt](android_native/app/src/main/java/com/example/plomaap/ui/screens/auth/RegisterScreen.kt). Permite:

- nombre completo
- correo
- teléfono
- contraseña
- dirección
- selección de rol: cliente o técnico

Los datos se envían al backend en el endpoint de registro mediante la API definida en [android_native/app/src/main/java/com/example/plomaap/data/api/ApiService.kt](android_native/app/src/main/java/com/example/plomaap/data/api/ApiService.kt).

### 5.3 Servicios y catálogo
La vista principal de servicios está en [android_native/app/src/main/java/com/example/plomaap/ui/screens/home/ServicesScreen.kt](android_native/app/src/main/java/com/example/plomaap/ui/screens/home/ServicesScreen.kt). Aquí se muestra:

- búsqueda de servicios
- listado de categorías
- diagnóstico inteligente con IA
- navegación a detalles del servicio
- direccionamiento a ubicación, horario y técnico

La app consume endpoints como:

- GET /api/services
- GET /api/services/categories
- GET /api/services/{id}

### 5.4 Citas y reservas
La app cuenta con flujo de reserva y calendario para crear citas. En la navegación principal y las pantallas de booking se manejan:

- selección de servicio
- ubicación del cliente
- horario disponible
- técnico disponible
- resumen final

Esto está organizado en:

- [android_native/app/src/main/java/com/example/plomaap/ui/navigation/AppNavigation.kt](android_native/app/src/main/java/com/example/plomaap/ui/navigation/AppNavigation.kt)
- [android_native/app/src/main/java/com/example/plomaap/viewmodel/BookingViewModel.kt](android_native/app/src/main/java/com/example/plomaap/viewmodel/BookingViewModel.kt)

### 5.5 Perfil de usuario
La pantalla de perfil se encuentra en [android_native/app/src/main/java/com/example/plomaap/ui/screens/profile/ProfileScreen.kt](android_native/app/src/main/java/com/example/plomaap/ui/screens/profile/ProfileScreen.kt). Permite:

- visualizar información del usuario
- ver correo, teléfono y dirección
- cerrar sesión
- editar perfil

## 6. Conexión con el backend Flask

La comunicación con el backend se centraliza en [android_native/app/src/main/java/com/example/plomaap/data/api/ApiClient.kt](android_native/app/src/main/java/com/example/plomaap/data/api/ApiClient.kt). El cliente usa Retrofit y un cliente OkHttp con:

- timeout de conexión y lectura
- logging de operaciones HTTP
- fallback de IP para pruebas locales y red Wi‑Fi

El cliente usa estas direcciones principales:

- 127.0.0.1:5000 para pruebas con USB ADB reverse
- 192.168.29.110:5000 para red Wi‑Fi local
- 10.0.2.2:5000 para emuladores Android

Esto es un punto importante para la exposición: la app no tiene una URL fija “mágica”; tiene una estrategia de resolución de red para que el móvil pueda conectarse al backend.

## 7. Roles y permisos

La app y el backend contemplan roles como:

- cliente
- técnico
- administrador

Estos roles se gestionan en el backend mediante JWT y validaciones de acceso. La app utiliza la sesión del usuario guardada localmente para identificar el usuario autenticado y decidir qué interfaz mostrar.

La lógica de roles está presente en:

- [android_native/app/src/main/java/com/example/plomaap/data/model/User.kt](android_native/app/src/main/java/com/example/plomaap/data/model/User.kt)
- [android_native/app/src/main/java/com/example/plomaap/data/repository/AuthRepository.kt](android_native/app/src/main/java/com/example/plomaap/data/repository/AuthRepository.kt)
- [android_native/app/src/main/java/com/example/plomaap/viewmodel/AuthViewModel.kt](android_native/app/src/main/java/com/example/plomaap/viewmodel/AuthViewModel.kt)

## 8. Endpoints principales consumidos por Android

La app consume varios endpoints del backend Flask:

- POST /api/auth/login
- POST /api/auth/google-login
- POST /api/auth/register
- POST /api/auth/forgot-password
- POST /api/auth/reset-password
- GET /api/auth/profile
- PATCH /api/auth/profile
- POST /api/auth/logout
- GET /api/services
- GET /api/services/{id}
- GET /api/technicians
- GET /api/technicians/available
- GET /api/technicians/slots
- GET /api/appointments
- POST /api/appointments
- GET /api/admin/dashboard
- GET /api/health

La firma exacta de estos endpoints se ve en [android_native/app/src/main/java/com/example/plomaap/data/api/ApiService.kt](android_native/app/src/main/java/com/example/plomaap/data/api/ApiService.kt).

## 9. Datos locales y persistencia

La app guarda la sesión en DataStore: 

- token JWT
- datos del usuario
- estado biométrico

Esto está en [android_native/app/src/main/java/com/example/plomaap/data/repository/AuthRepository.kt](android_native/app/src/main/java/com/example/plomaap/data/repository/AuthRepository.kt). Esto permite mantener una sesión activa y evitar que el usuario tenga que volver a iniciar sesión cada vez.

## 10. Repositorio de la API y documentacion

El backend Flask tiene documentación y archivos de referencia, entre ellos:

- README.md
- docs/QUICK_REFERENCE.md
- docs/BACKEND_DESIGN.md
- docs/ARCHITECTURE_FLOWS.md
- postman_collection_plomapp_backend.json

La documentación está en el backend, no en el proyecto Android. El proyecto Android sí consume la API y usa sus modelos, pero la infraestructura documentada está en el backend.

## 11. Estado de Swagger/OpenAPI

No se encontró evidencia de Swagger u OpenAPI en la estructura revisada del proyecto Android o del backend. Lo que sí existe es:

- documentación Markdown
- colección de Postman
- rutas y pruebas del backend

Por lo tanto, para una defensa estricta, conviene decirlo así: el backend está documentado, pero la especificación Swagger/OpenAPI no está implementada explícitamente como archivo visible.

## 12. Metodología ágil

La app Android presenta módulos claramente divididos por funcionalidad: autenticación, home, citas, perfil, técnicos y navegación. Eso permite estructurarlas como historias de usuario, por ejemplo:

- Como cliente quiero iniciar sesión.
- Como cliente quiero buscar servicios.
- Como cliente quiero agendar una cita.
- Como técnico quiero consultar mis citas.
- Como administrador quiero revisar el dashboard.

En la práctica, la app tiene un diseño compatible con metodología ágil, pero la evidencia documental de sprints, tablero o backlog no aparece como artefacto verificable en el código revisado.

## 13. Control de versiones

El repositorio del backend tiene historial Git y ramas visibles con commits, mientras que la app Android muestra un proyecto más activo con varias modificaciones recientes. En exposición, conviene mostrar:

- git log --oneline
- git branch --all
- git status

Esto permite evidenciar la evolución del proyecto y la trazabilidad del desarrollo.

## 14. Orden recomendado para la exposición

1. Presentar la idea del proyecto PlomApp.
2. Explicar la arquitectura Android + Flask.
3. Mostrar el flujo de login.
4. Mostrar registro con roles.
5. Mostrar catálogo de servicios.
6. Mostrar selección de ubicación, horario y técnico.
7. Mostrar creación de cita.
8. Mostrar perfil, edición y cierre de sesión.
9. Mostrar documentación del backend y endpoints principales.
10. Mostrar Git y el historial de cambios.

## 15. Conclusión

El proyecto Android Native desarrollado en Kotlin y Compose es una aplicación funcional y bien estructurada para gestionar servicios de plomería. Tiene una arquitectura organizada, se conecta con un backend Flask via Retrofit, maneja sesiones con JWT, integra autenticación por correo, Google y Passkey, y presenta una experiencia completa de navegación, servicios, citas y perfil de usuario.

La parte más sólida es la integración real con la API y la organización modular de la aplicación. Los puntos que requieren mayor cuidado en defensa son la documentación Swagger/OpenAPI y la evidencia formal de metodología ágil, si se exige un nivel de rigor más alto por parte del evaluador.
