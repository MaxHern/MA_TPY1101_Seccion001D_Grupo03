VeciZervi
Conectando comunidades a través de soluciones simples.

## 📱 Descarga la Aplicación
¡Prueba la versión oficial de VeciZervi en tu dispositivo Android!

[![Descargar APK](https://img.shields.io/badge/Descargar-APK-green?style=for-the-badge&logo=android)](https://github.com/MaxHern/MA_TPY1101_Seccion001D_Grupo03/releases/download/v1.0.0/app-debug.1.apk)

> **Nota para la instalación:** Como es una aplicación en fase de pruebas (Sprint 1), es posible que tu teléfono solicite permisos para "Instalar aplicaciones de orígenes desconocidos". Debes permitirlo para continuar con la instalación.

VeciZervi es una aplicación móvil tipo marketplace vecinal que conecta a personas que necesitan ayuda con tareas cotidianas ("pololos") con vecinos dispuestos a ofrecer sus servicios de forma rápida, cercana y confiable.

Proyecto desarrollado para la asignatura TPY1101 — Taller Aplicado de Programación, Sección 001D, Grupo 03 (DUOC UC).

¿Qué problema resuelve?
En los barrios es difícil encontrar ayuda rápida y de confianza para labores domésticas (fletes, aseo, jardinería, reparaciones, etc.). Las personas no saben a quién recurrir dentro de su propio entorno, y quienes ofrecen estos servicios no cuentan con una plataforma para darse a conocer de manera segura.

VeciZervi resuelve esto entregando un espacio digital donde la comunidad puede:

Publicar requerimientos — crear anuncios con título, descripción, precio y categoría del trabajo.
Ver oportunidades cercanas — explorar los trabajos disponibles dentro de un radio próximo a la ubicación del usuario.
Contactar de forma directa — coordinar los detalles mediante un chat interno seguro, sin exponer datos personales.
Generar confianza — verificación de identidad, estados de trabajo (asignado / finalizado) y calificaciones entre vecinos.
¿Cómo lo hicimos?
El proyecto sigue una arquitectura cliente–servidor de tres capas, completamente desplegada en la nube.

Capa	Tecnología
Frontend (móvil)	Kotlin + Jetpack Compose (Android Studio), consumo de API con Retrofit
Backend (API REST)	Java 21 + Spring Boot, Spring Data JPA, Hibernate, HikariCP, BCrypt — desplegado en Render (PaaS)
Base de datos	PostgreSQL alojada en Aiven (DBaaS), migrada desde MySQL local
Control de versiones	Git + GitHub
Gestión ágil	Jira (Scrum)
Flujo: App Android → API en Render → PostgreSQL en Aiven.

Metodología
Trabajamos con Scrum, organizando el desarrollo en sprints gestionados en Jira. Definimos 5 épicas y 16 historias de usuario que cubren identidad y seguridad, el marketplace de trabajos, geolocalización, comunicación por chat y el sistema de confianza y cierre.

Equipo
Integrante	Roles
Maximiliano Hernández	Scrum Master · DBA · Líder de QA
Iván Morales	Product Owner · Frontend · QA
Diego Zamora	Backend · API REST · Seguridad
Plan de pruebas
La calidad del sistema se validó con un plan documentado bajo el estándar internacional ISO/IEC/IEEE 29119-3, con criterios de entrada y salida aprobados por el Test Manager. Se cubrieron tres frentes:

1. Pruebas funcionales
30 escenarios de prueba (casos ideales y de excepción) sobre registro, login, publicación, listados, cambios de estado y chat.
Resultado: 28 de 30 aprobados (93%). Quedó pendiente el badge de mensajes no leídos, que requiere un nuevo campo en la base de datos (planificado para un sprint futuro).
Herramientas: Postman v11, emulador Android API 34 y dispositivo real Samsung Galaxy A54.
2. Pruebas de rendimiento (Apache JMeter 5.6.3)
Carga y estrés sobre la API en Render y la base de datos en Aiven:

ID	Escenario	Resultado
R01	Estrés de autenticación (50 usuarios)	100% exitoso, 0 errores HTTP 503
R02	Carga de mensajería concurrente	ACID verificado, sin deadlocks
R03	Saturación por polling	99.8% (499/500), prom. 1.8s
R04	Lectura del muro (100 hilos)	100% exitoso, alta disponibilidad
Los tiempos de respuesta elevados se explican por el cold-start del plan gratuito de Render y el cálculo criptográfico BCrypt; en ningún caso el servicio dejó de responder.

3. Pruebas de seguridad (OWASP Top 10)
Auditoría proactiva con 5 mitigaciones aprobadas:

ID	Vulnerabilidad	Mitigación
S01	Fuerza bruta	Bloqueo de cuenta tras 3 intentos fallidos (15 min)
S02	Inyección SQL	Consultas parametrizadas con Spring Data JPA
S03	Exposición de datos sensibles	@JsonIgnoreProperties oculta RUT, hash y correo
S04	Broken Access Control	Rutas protegidas con JWT (HTTP 401 sin sesión)
S05	Almacenamiento inseguro	Contraseñas cifradas con BCrypt (irreversibles)
Repositorio
Código fuente público: https://github.com/MaxHern/MA_TPY1101_Seccion001D_Grupo03

Proyecto académico — DUOC UC · TPY1101 · Sección 001D · Grupo 03.
