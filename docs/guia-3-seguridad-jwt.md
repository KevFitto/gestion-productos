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

## Paso 3. Organización de archivos

Se crearon las seis clases vacías de la estructura propuesta dentro del paquete
principal existente. Se utiliza controller y service en singular para mantener
la organización del proyecto; equivalen a controllers y services de la guía.

```text
ni/edu/uam/gestionproductos/
├── config/SecurityConfig.java
├── controller/AuthController.java
├── controller/PublicoController.java
├── dto/LoginRequest.java
├── dto/LoginResponse.java
├── service/TokenService.java
├── controller/ProductoController.java   (existente)
└── controller/CategoriaController.java  (existente)
```

Los controladores CRUD y los demás archivos existentes se conservan.
Los campos, métodos y anotaciones de las nuevas clases se agregarán conforme
a los siguientes pasos. Esta organización todavía no cambia la seguridad
ni agrega endpoints. Verificación: compilación con Maven.

## Paso 4. Configurar el secreto JWT

Se generaron 32 bytes aleatorios mediante el generador criptográfico
System.Security.Cryptography.RandomNumberGenerator y se codificaron en Base64,
equivalente al generador Java temporal propuesto.

La variable JWT_SECRET_BASE64 se guardó únicamente en una configuración local
de IntelliJ llamada **GestionProductosApplication JWT**, basada en la
configuración de Spring Boot existente. Su archivo está dentro de
.idea/runConfigurations/, excluido de Git por la regla .idea del proyecto.
El valor no se imprime ni se incluye en esta documentación.

Para ejecutar desde IntelliJ, seleccionar **GestionProductosApplication JWT**
en el selector Run/Debug. Si la aplicación ya está ejecutándose, detenerla
antes de iniciar esta configuración para evitar ocupar dos veces el puerto
8080. Reiniciar la aplicación después de cambiar la variable.

Se verificó que el valor Base64 guardado decodifica a 32 bytes y que Git ignora
el archivo local. No se agregaron secretos a application.properties ni al
código. La lectura de la variable y la firma/validación HS256 se implementarán
en los siguientes pasos; todavía no se utilizan tokens.

En otra computadora se debe generar una clave nueva y configurar la variable
en Run/Debug. No compartir el XML local ni capturas que muestren la clave.
