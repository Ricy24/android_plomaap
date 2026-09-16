# Informe de exposicion - PlomApp

## 1. Datos generales

**Proyecto:** PlomApp - aplicacion movil de servicios de plomeria y mantenimiento  
**Cliente movil:** Flutter  
**Backend:** API REST desarrollada con Flask  
**Backend local:** `C:\Users\Anrid\plomaap_react_estable\backend_flask`  
**Servidor esperado:** `http://localhost:5000` para navegador o `http://<IP-del-PC>:5000` para dispositivo movil.

## 2. Objetivo del proyecto

PlomApp conecta clientes que necesitan servicios de plomeria con tecnicos disponibles. El usuario puede autenticarse, seleccionar un rol, consultar servicios, revisar tecnicos, agendar citas, consultar sus solicitudes y actualizar su perfil. El backend centraliza la autenticacion, las reglas de negocio, la persistencia y el control de permisos.

## 3. Resumen de cumplimiento

| Criterio | Evidencia encontrada | Estado para exponer |
|---|---|---|
| Codificacion completa y modulos funcionando | Login, registro, roles, servicios, tecnicos, citas, perfil y dashboard administrativo | Implementado en cliente y backend; demostrar flujo real |
| Consumo real de API REST | `ApiService` usa HTTP contra Flask y envia JWT Bearer | Implementado; desactivar o identificar el fallback mock durante la prueba |
| API REST documentada | README, `docs/QUICK_REFERENCE.md`, documentos de arquitectura y coleccion Postman | Documentada; no se encontro Swagger/OpenAPI visible |
| Metodologia agil | El codigo no contiene backlog, historias, sprints o actas verificables | Preparar evidencia del proceso o presentarlo como proceso aplicado |
| Control de versiones | Historial Git del backend, remoto `origin/main` y rama `main` | Evidencia parcial en movil; backend tiene historial mas completo |

## 4. Criterio VI: codificacion al 100%

### 4.1 Autenticacion y usuarios

La aplicacion movil contiene pantallas de inicio de sesion, registro y recuperacion de contrasena. El registro permite seleccionar el rol `customer` o `technician`.

**Evidencia en el movil:**

- `lib/screens/login_screen.dart`: formulario de autenticacion y navegacion al inicio.
- `lib/screens/register_screen.dart`: alta de usuario con nombre, correo, contrasena, telefono, direccion y rol.
- `lib/providers/auth_provider.dart`: administra token, sesion, perfil, logout y persistencia local.
- `lib/services/api_service.dart`: llamadas a `/api/auth/login`, `/api/auth/register`, `/api/auth/profile` y recuperacion de contrasena.

**Evidencia en Flask:**

- `app/routes/auth.py`: endpoints de autenticacion y perfil.
- JWT para proteger recursos y bcrypt para contrasenas.
- El backend retorna usuario y token para que el cliente conserve la sesion.

### 4.2 Servicios, tecnicos y citas

La pantalla principal ofrece cuatro areas: servicios, mis citas, tecnicos y perfil.

**Flujo que se puede demostrar:**

1. Iniciar sesion.
2. Consultar el catalogo de servicios.
3. Abrir el detalle de un servicio.
4. Crear una cita indicando fecha, hora y observaciones.
5. Consultar las citas del usuario.
6. Consultar tecnicos disponibles.
7. Editar el perfil y cerrar sesion.

**Endpoints principales:**

| Modulo | Operaciones |
|---|---|
| Auth | `POST /api/auth/login`, `POST /api/auth/register`, `GET/PATCH /api/auth/profile` |
| Servicios | `GET /api/services`, `GET /api/services/<id>` |
| Tecnicos | `GET /api/technicians`, disponibilidad, horarios y citas del tecnico |
| Citas | `GET/POST /api/appointments`, `GET/PATCH /api/appointments/<id>` |
| Administracion | dashboard, usuarios y actualizacion de citas |
| Salud | `GET /api/health` |

Esto cubre operaciones de consulta y creacion en el flujo movil, y operaciones de actualizacion en perfil, citas y administracion. El backend tambien contiene rutas CRUD para hogares, habitaciones y activos en `app/routes/homes.py`.

### 4.3 Reportes y administracion

El backend incluye `app/routes/admin.py` con el endpoint `GET /api/admin/dashboard`, ademas de gestion de usuarios y citas. En la exposicion debe mostrarse la respuesta del dashboard o una captura del panel administrativo, porque la aplicacion Flutter visible esta enfocada principalmente en el flujo de cliente.

### 4.4 Roles y permisos

El sistema contempla los roles `customer`, `technician` y `admin`. Los decoradores y validaciones JWT restringen las operaciones segun el rol. En el modulo de hogares, por ejemplo, el propietario y el administrador pueden modificar, mientras que un miembro puede consultar sin modificar.

**Demostracion recomendada:**

- Entrar como cliente y crear una cita.
- Entrar como tecnico y mostrar sus citas o disponibilidad.
- Entrar como administrador y mostrar dashboard, usuarios o actualizacion de una cita.
- Intentar una operacion no permitida y mostrar el `403 Forbidden`.

## 5. Criterio VI: consumo real de API REST en movil

El cliente Flutter utiliza el paquete `http` y centraliza las solicitudes en `lib/services/api_service.dart`. La URL se define segun la plataforma:

- Web: `http://localhost:5000`.
- Android, iOS o Windows: IP del equipo que ejecuta Flask en el puerto `5000`.

Las solicitudes protegidas envian el encabezado:

```text
Authorization: Bearer <jwt>
```

### Prueba tecnica para la exposicion

1. Activar el entorno virtual del backend:

```powershell
cd C:\Users\Anrid\plomaap_react_estable\backend_flask
.\venv\Scripts\Activate.ps1
python run.py
```

2. Comprobar salud del servidor:

```powershell
Invoke-RestMethod http://localhost:5000/api/health
```

3. Ejecutar la app Flutter desde `C:\Users\Anrid\Documents\plomaap`.

4. Abrir las herramientas de red o los logs del backend y mostrar las peticiones a `/api/auth`, `/api/services` y `/api/appointments`.

### Observacion importante

`ApiService` contiene respuestas mock para cuando Flask no esta disponible. Esto permite que la interfaz no se bloquee, pero para cumplir el requisito de consumo real hay que mantener Flask encendido, usar una IP accesible desde el dispositivo y mostrar la respuesta proveniente del servidor. En la defensa se debe declarar que el mock es solo respaldo de desarrollo, no la evidencia principal.

## 6. Criterio VI: API REST documentada

El backend tiene documentacion en:

- `README.md`: instalacion, arquitectura, caracteristicas y endpoints principales.
- `docs/QUICK_REFERENCE.md`: modelos, autenticacion, endpoints, validaciones y respuestas.
- `docs/STRUCTURE.md`: organizacion del backend.
- `docs/BACKEND_DESIGN.md`: especificacion tecnica.
- `docs/ARCHITECTURE_FLOWS.md`: flujos de arquitectura.
- `postman_collection_plomapp_backend.json`: coleccion utilizable para probar la API.

**Resultado:** la API esta documentada mediante Markdown y Postman. No se encontro una interfaz Swagger ni un archivo OpenAPI en los artefactos revisados. Si el evaluador exige literalmente Swagger, el pendiente es agregar una especificacion OpenAPI o integrar Flask-Smorest/Flasgger y presentar su URL.

## 7. Criterio VI: metodologia agil

El producto presenta funcionalidades que pueden organizarse como historias de usuario:

| Historia | Criterio de aceptacion |
|---|---|
| Como cliente quiero iniciar sesion | El backend valida credenciales y devuelve JWT |
| Como cliente quiero consultar servicios | La app muestra datos obtenidos desde `/api/services` |
| Como cliente quiero agendar una cita | El backend crea la cita y la app la muestra en mis solicitudes |
| Como tecnico quiero consultar mis citas | Solo el tecnico autenticado accede a su agenda |
| Como administrador quiero revisar el estado | El dashboard retorna indicadores y usuarios/citas |

### Guion de sprints para explicar el proceso

- **Sprint 1:** estructura del proyecto, autenticacion y registro.
- **Sprint 2:** catalogo de servicios y tecnicos.
- **Sprint 3:** agenda, citas y perfiles.
- **Sprint 4:** roles, dashboard, validaciones, pruebas y documentacion.

Para que este criterio quede plenamente demostrable, anexar el tablero usado por el equipo con backlog, historias, responsables, estados, fechas y capturas de cada sprint. En los archivos revisados no se encontro un tablero o acta agil verificable, por lo que no conviene afirmar que existe esa evidencia si no se presenta aparte.

## 8. Criterio VI: control de versiones

### Backend

El repositorio del backend tiene historial de commits, rama `main` y remoto `origin/main`. Algunos commits visibles son:

- `c5ec3cb` - Complemento de dashboard admin.
- `2b354c5` - Correccion de emojis.
- `adf1991` - Arreglo de versiones.
- `013a5b2` - Actualizacion backend.
- `20b270f` - Implementacion de backend Flask.

### Aplicacion movil

El repositorio Flutter tiene la rama `main` y el commit inicial `db8985e` - Primer commit de PlomApp. Para demostrar ramas en la entrega, se recomienda crear ramas de trabajo con nombres claros, por ejemplo `feature/autenticacion`, `feature/citas` y `docs/informe`, y fusionarlas mediante pull request o merge documentado. No se deben inventar capturas ni commits que no existan.

### Comandos para mostrar la evidencia

```powershell
git log --oneline --decorate --graph --all
git branch --all
git remote -v
git status
```

Ejecutar estos comandos tanto en el movil como en el backend y capturar la terminal completa.

## 9. Orden recomendado para la exposicion

1. Presentar el problema y el objetivo de PlomApp.
2. Mostrar la arquitectura: Flutter -> API Flask -> base de datos.
3. Levantar Flask y comprobar `/api/health`.
4. Ejecutar login y registro con roles.
5. Consultar servicios y tecnicos desde el movil.
6. Crear y consultar una cita.
7. Mostrar el perfil y el cierre de sesion.
8. Mostrar dashboard y permisos administrativos.
9. Abrir la documentacion Markdown y la coleccion Postman.
10. Mostrar Git: commits, rama, remoto y estado.
11. Explicar el backlog y los sprints con el tablero agil anexado.

## 10. Conclusiones

PlomApp cuenta con una base funcional de cliente movil y una API Flask modular. Se evidencian autenticacion JWT, roles, catalogo de servicios, tecnicos, citas, perfil, administracion, pruebas de contrato y documentacion tecnica. La demostracion debe ejecutarse con el backend activo para probar consumo real y debe diferenciar las respuestas mock de las respuestas de Flask.

Los dos puntos que requieren evidencia adicional para una evaluacion estricta son la interfaz Swagger/OpenAPI y el proceso agil documentado. Tambien es recomendable mostrar ramas de trabajo reales en el repositorio movil, ya que actualmente la evidencia visible se concentra en la rama `main`.
