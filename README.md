PlomApp

PlomApp es una aplicación móvil desarrollada en Flutter para la gestión de servicios de plomería. Permite a los clientes solicitar servicios y realizar seguimiento de sus solicitudes.

Tecnologías
Flutter
Dart
Python
Flask
MySQL
API REST
Git y GitHub


Funcionalidades
Registro de usuarios
Inicio de sesión
Gestión de perfiles
Solicitud de servicios
Consulta de solicitudes
Seguimiento del estado del servicio
Asignación de técnicos
Gestión de usuarios y servicios

Arquitectura

El proyecto utiliza la siguiente estructura:

Flutter
   |
   | API REST
   |
Flask
   |
   |
MySQL

La aplicación Flutter se comunica con el backend mediante peticiones HTTP y datos en formato JSON.

API REST

La aplicación consume una API REST desarrollada con Flask.

Algunos de los endpoints utilizados son:

POST /api/login
POST /api/usuarios
GET  /api/usuarios/{id}
GET  /api/servicios
POST /api/solicitudes
GET  /api/solicitudes
PUT  /api/solicitudes/{id}
Documentación de la API

La API REST se encuentra documentada mediante Swagger/OpenAPI.

http://localhost:5000/docs

La dirección puede variar dependiendo de la configuración del servidor.

Metodología de desarrollo

El proyecto se desarrolla utilizando una metodología ágil, teniendo en cuenta:

Historias de usuario
Product Backlog
Roles
Sprints
Pruebas
Seguimiento del desarrollo
Instalación

Clonar el repositorio:

git clone https://github.com/Ricy24/android_plomaap.git

Ingresar al proyecto:

cd android_plomaap

Instalar las dependencias:

flutter pub get

Ejecutar la aplicación:

flutter run
Requisitos

Para ejecutar el proyecto se necesita:

Flutter
Dart
Android Studio o Visual Studio Code
Android SDK
Python
MySQL o MariaDB
Presentación

Las diapositivas del proyecto se encuentran en:

https://canva.link/xpyafyytoah9hkm

Proyecto académico

Proyecto desarrollado con fines académicos para la gestión de servicios de plomería mediante una aplicación móvil y una API REST.
