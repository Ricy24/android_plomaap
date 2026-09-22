# 🚰 **PlomApp**

**PlomApp** es una aplicación móvil desarrollada en **Flutter** para la gestión de servicios de plomería. Permite a los clientes solicitar servicios, consultar sus solicitudes y realizar seguimiento al estado de los servicios.

---

## 🛠️ **Tecnologías utilizadas**

* **Flutter**
* **Dart**
* **Python**
* **Flask**
* **MySQL / MariaDB**
* **API REST**
* **Swagger / OpenAPI**
* **Git y GitHub**

---

## 📱 **Funcionalidades**

* 👤 **Registro de usuarios**
* 🔐 **Inicio de sesión**
* 📝 **Gestión de perfiles**
* 🔧 **Solicitud de servicios**
* 📋 **Consulta de solicitudes**
* 📊 **Seguimiento del estado del servicio**
* 👨‍🔧 **Asignación de técnicos**
* ⚙️ **Gestión de usuarios y servicios**

---

## 🏗️ **Arquitectura**

El proyecto está compuesto por una aplicación móvil, una API REST y una base de datos.

```text
📱 Flutter
     |
     | HTTP / JSON
     ▼
🌐 API REST - Flask
     |
     ▼
🗄️ MySQL / MariaDB
```

La aplicación Flutter se comunica con el backend mediante peticiones **HTTP** y datos en formato **JSON**.

---

## 🔌 **API REST**

PlomApp utiliza una **API REST desarrollada con Flask** para la comunicación entre la aplicación móvil y el servidor.

Algunos de los endpoints utilizados son:

```text
POST /api/login
POST /api/usuarios
GET  /api/usuarios/{id}
GET  /api/servicios
POST /api/solicitudes
GET  /api/solicitudes
PUT  /api/solicitudes/{id}
```

---

## 📚 **Documentación Swagger**

La API REST cuenta con documentación mediante **Swagger / OpenAPI**.

```text
http://localhost:5000/docs
```

Desde Swagger se pueden consultar y probar los diferentes endpoints de la API.

---

## 📋 **Metodología ágil**

El desarrollo de **PlomApp** se realiza utilizando una metodología ágil, teniendo en cuenta:

* 📌 **Historias de usuario**
* 📋 **Product Backlog**
* 👥 **Roles**
* 🔄 **Sprints**
* 🧪 **Pruebas**
* 📊 **Seguimiento del proyecto**

---

## 🚀 **Instalación**

### 1. Clonar el repositorio

```bash
git clone https://github.com/Ricy24/android_plomaap.git
```

### 2. Ingresar al proyecto

```bash
cd android_plomaap
```

### 3. Instalar las dependencias

```bash
flutter pub get
```

### 4. Ejecutar la aplicación

```bash
flutter run
```

---

## 💻 **Requisitos**

Para ejecutar el proyecto se necesita:

* **Flutter**
* **Dart**
* **Android Studio o Visual Studio Code**
* **Android SDK**
* **Python**
* **MySQL o MariaDB**

---

## 📽️ **Presentación**

Las diapositivas del proyecto se encuentran disponibles en:

👉 https://canva.link/xpyafyytoah9hkm

---

## 🎓 **Proyecto académico**

**PlomApp** es un proyecto académico desarrollado para la gestión de servicios de plomería mediante una aplicación móvil, una **API REST** y una **base de datos**.

### 👨‍💻 **Equipo de desarrollo**

Proyecto desarrollado como parte del proceso de formación académica.

---

## 📄 **Licencia**

Este proyecto fue desarrollado con **fines académicos y educativos**.
