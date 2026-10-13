# Guía de seguridad JWT

## Paso 2. Preparación del entorno

- Rama de trabajo: seguridad-jwt.
- Antes de agregar seguridad, GET http://localhost:8080/api/productos respondió
  HTTP 200 con tres productos.
- Se conserva Java 21 en pom.xml (el entorno ejecuta JDK 25).
- Por decisión del estudiante se conserva Spring Boot 4.1.1 y se adaptarán
  los ejemplos de Boot 3.x/Security 6.x a las versiones administradas por Maven.
  El BOM de Boot 4.1.1 administra Spring Security 7.1.1.
- Se conserva el paquete ni.edu.uam.gestionproductos; las referencias a
  com.example.factura en los ejemplos se ajustarán a este paquete.
- Se agregaron spring-boot-starter-security y
  spring-boot-starter-oauth2-resource-server dentro del bloque dependencies
  existente, sin fijar versiones independientes.
- Se conservan las dependencias de Web, JPA, Flyway y PostgreSQL y la base
  de datos existente.

Agregar las dependencias activa la seguridad predeterminada de Spring Boot.
Esta etapa todavía no implementa JWT, /api/auth/login, /api/publico/saludo
ni las reglas USER/ADMIN. Las peticiones anónimas del CRUD pueden ser
rechazadas o redirigidas al formulario de acceso según el tipo de petición.
Las pruebas HTTP del CRUD anterior deberán incorporar autenticación y
ajustarse a las reglas que se implementen en los siguientes pasos.

Referencia: https://docs.spring.io/spring-boot/reference/web/spring-security.html

Verificación de esta etapa:

- Maven compiló el código principal y las pruebas.
- GestionProductosApplicationTests y ProductoServiceIntegrationTests:
  cuatro pruebas aprobadas, sin errores.
- La aplicación arrancó en 8080 con las nuevas dependencias.
- GET /api/productos sin credenciales, con Accept: application/json:
  HTTP 401, como corresponde a la seguridad predeterminada.
- Se detuvo la instancia temporal para liberar 8080.
- No se ejecutó la suite HTTP anterior en esta etapa; sus peticiones sin
  credenciales ya no representan el contrato de la API protegida.
