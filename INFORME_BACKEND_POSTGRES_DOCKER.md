# Informe del backend PlomApp con PostgreSQL en Docker

## 1. Información general

**Proyecto:** PlomApp  
**Backend:** Flask API REST  
**Base de datos activa:** PostgreSQL  
**Motor configurado:** PostgreSQL en Docker  
**Ruta del backend:** C:\Users\Anrid\plomaap_react_estable\backend_flask  
**Configuración activa detectada:**

- DB_ENGINE=postgresql
- POSTGRES_HOST=localhost
- POSTGRES_PORT=5434
- POSTGRES_DB=plomapp

Esto se observa directamente en el archivo de configuración del backend:

- [backend_flask/.env](../backend_flask/.env)
- [backend_flask/app/config/__init__.py](../backend_flask/app/config/__init__.py)

## 2. Objetivo del backend

El backend de PlomApp tiene como finalidad centralizar la lógica del negocio, la autenticación, la gestión de usuarios, servicios, citas, técnicos, administración y datos del hogar. Su función principal es servir como API REST para la aplicación móvil y sincronizar todo el flujo operativo con una base de datos persistente.

## 3. Arquitectura del backend

El backend está estructurado de forma modular, separando responsabilidades por carpetas:

- app/
  - routes/: blueprints de endpoints
  - database/: modelos y conexiones
  - controllers/: lógica de negocio
  - services/: servicios auxiliares
  - utils/: validaciones y utilidades
  - config/: configuración de entorno

La app usa Flask con factory pattern, carga la configuración según el entorno y genera la URL de conexión a la base de datos según las variables de entorno.

## 4. Base de datos: PostgreSQL en Docker

La configuración actual del backend confirma que PostgreSQL es la base de datos activa y no MySQL:

```env
DB_ENGINE=postgresql
POSTGRES_USER=postgres
POSTGRES_PASSWORD=Riki1049616429
POSTGRES_HOST=localhost
POSTGRES_PORT=5434
POSTGRES_DB=plomapp
```

Además, el archivo de configuración interpreta esta información y arma la cadena de conexión de esta forma:

```python
if db_engine in ['postgres', 'postgresql'] or os.getenv('POSTGRES_DB'):
    user = os.getenv('POSTGRES_USER', 'postgres')
    password = os.getenv('POSTGRES_PASSWORD', '')
    host = os.getenv('POSTGRES_HOST', 'localhost')
    port = os.getenv('POSTGRES_PORT', '5432')
    db_name = os.getenv('POSTGRES_DB', 'plomapp')
    return f"postgresql+psycopg2://{user}:{password}@{host}:{port}/{db_name}"
```

Esto demuestra que la conexión activa es PostgreSQL, no MySQL. El puerto configurado para la base de datos en Docker es 5434, por lo que la persistencia está corriendo sobre PostgreSQL dentro de un contenedor Docker y la app Flask apunta a ese servicio local.

## 5. Evidencias de la estructura

### 5.1 Configuración de PostgreSQL

- [backend_flask/.env](../backend_flask/.env)
- [backend_flask/app/config/__init__.py](../backend_flask/app/config/__init__.py)

### 5.2 Documentación del backend

- [backend_flask/README.md](../backend_flask/README.md)
- [backend_flask/docs/QUICK_REFERENCE.md](../backend_flask/docs/QUICK_REFERENCE.md)
- [backend_flask/docs/BACKEND_DESIGN.md](../backend_flask/docs/BACKEND_DESIGN.md)
- [backend_flask/docs/STRUCTURE.md](../backend_flask/docs/STRUCTURE.md)

### 5.3 Pruebas y API

- [backend_flask/tests/test_api_contract.py](../backend_flask/tests/test_api_contract.py)
- [backend_flask/postman_collection_plomapp_backend.json](../backend_flask/postman_collection_plomapp_backend.json)

## 6. Módulos principales del backend

### 6.1 Autenticación
El backend expone endpoints para:

- login
- registro
- recuperación de contraseña
- perfil de usuario
- cierre de sesión
- verification y passkeys

La autenticación usa JWT y la app cliente envía el token como:

```http
Authorization: Bearer <token>
```

### 6.2 Servicios
Módulo encargado de gestionar el catálogo de servicios disponibles para la app móvil.

Endpoints principales:

- GET /api/services
- GET /api/services/<id>
- GET /api/services/categories

### 6.3 Técnicos
Este módulo gestiona información de técnicos, disponibilidad y citas.

Endpoints principales:

- GET /api/technicians
- GET /api/technicians/available
- GET /api/technicians/slots
- GET /api/technicians/profile
- GET /api/technicians/appointments
- PATCH /api/technicians/appointments/<id>

### 6.4 Citas
El backend procesa las reservas y la lógica de disponibilidad de servicios.

Endpoints principales:

- GET /api/appointments
- POST /api/appointments
- GET /api/appointments/<id>
- PATCH /api/appointments/<id>

### 6.5 Administración
Módulo para dashboard, usuarios y control de citas.

Endpoints principales:

- GET /api/admin/dashboard
- GET /api/admin/users
- PATCH /api/admin/users/<id>
- PATCH /api/admin/appointments/<id>

### 6.6 Hogar Digital
Además de la parte principal del negocio, el backend incluye módulos para hogar digital, habitaciones y activos.

Ruta importante:

- [backend_flask/app/routes/homes.py](../backend_flask/app/routes/homes.py)

## 7. Seguridad y autenticación

El backend aplica una arquitectura de seguridad basada en:

- JWT para autenticación de rutas protegidas
- validación de usuarios y roles
- control de acceso por permisos
- manejo de errores HTTP (400, 401, 403, 404, 500)
- conexión segura a la base de datos con variables de entorno

Esto permite proteger rutas sensibles y evitar acceso no autorizado a información del usuario, los servicios o las citas.

## 8. Flujo de trabajo del backend

1. El cliente Android realiza una petición HTTP a la API Flask.
2. La app valida el cuerpo o parámetros de entrada.
3. El backend identifica al usuario con JWT o autentica la petición.
4. La lógica del negocio ejecuta la operación solicitada.
5. Se consultan o actualizan los datos en PostgreSQL.
6. La API devuelve un JSON con la respuesta del proceso.

## 9. Ventajas de usar PostgreSQL con Docker

- portabilidad del entorno
- facilidad de replicar la base de datos
- aislamiento del servicio de base de datos
- fácil reinicio y limpieza del contenedor
- compatibilidad con datos estructurados y consultas más complejas

En este proyecto, PostgreSQL está funcionando como base activa y el backend se conecta a ella por el puerto 5434 local, lo cual resulta adecuado para el desarrollo y pruebas del sistema.

## 10. Estado actual

El backend está configurado para trabajar con PostgreSQL y no con MySQL. La evidencia más clara es la configuración activa en el archivo .env y en app/config/__init__.py. Por tanto, la base de datos que debe mencionarse en la exposición es PostgreSQL en Docker, con conexión local a localhost:5434.

## 11. Conclusión

El backend de PlomApp está bien estructurado, modular y orientado a servicios REST. Está diseñado para soportar autenticación, servicios, técnicos, citas, administración y hogar digital. La diferencia clave con versiones anteriores es que la base de datos activa es PostgreSQL en Docker, usando la configuración de entorno que apunta a localhost:5434. En la defensa se debe presentar esta configuración como parte central de la arquitectura, porque es la que realmente está activada en el proyecto.

## 12. Recomendación para la exposición

En la presentación del backend se recomienda mostrar:

1. la estructura del proyecto
2. la configuración de PostgreSQL
3. el archivo .env con la variable DB_ENGINE=postgresql
4. un endpoint funcionando como /api/health o /api/auth/login
5. la respuesta JSON de la API
6. la conexión con la app Android

## 13. Comandos de verificación recomendados

```powershell
cd C:\Users\Anrid\plomaap_react_estable\backend_flask
.\venv\Scripts\Activate.ps1
python run.py
```

Luego verificar:

```powershell
Invoke-RestMethod http://localhost:5000/api/health
```

Y revisar la base de datos configurada:

```powershell
Get-NetTCPConnection -LocalPort 5434 -State Listen
```

Esto permite confirmar que la API y la base de datos PostgreSQL en Docker están levantadas y accesibles.
