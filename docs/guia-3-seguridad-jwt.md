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

## Paso 5. Configurar Spring Security

SecurityConfig contiene la configuración del docente, cambiando el paquete
a ni.edu.uam.gestionproductos.config y omitiendo el import Customizer sin uso.
El constructor DaoAuthenticationProvider(UserDetailsService) y los componentes
JWT utilizados son compatibles con Spring Security 7.1.1.

- Usuarios de laboratorio en memoria: admin con rol ADMIN y operador con rol
  USER, usando las contraseñas del ejemplo del docente y BCrypt.
- AuthenticationManager verifica las credenciales.
- JWT_SECRET_BASE64 es obligatoria: se decodifica a una clave HmacSHA256 de
  al menos 32 bytes. Sin la variable, la aplicación no inicia.
- JwtEncoder firma tokens; JwtDecoder valida HS256 y vencimiento.
- El claim roles se convierte en autoridades con prefijo ROLE_.
- No se mantienen sesiones HTTP y se desactiva CSRF conforme al ejemplo.

| Ruta | Regla en este paso |
| --- | --- |
| /api/auth/** y /api/publico/** | Acceso anónimo permitido |
| GET /api/productos/** | USER o ADMIN |
| Otros métodos /api/productos/** | ADMIN |
| Cualquier otra ruta | Usuario autenticado |

Categorías todavía no tiene restricciones por rol; cualquier usuario autenticado
puede acceder a sus endpoints. Se implementarán esas restricciones en el reto
final, como indica la guía. Los controladores AuthController y PublicoController
continúan vacíos: permitir sus rutas no crea aún el login ni el saludo.

Se agregó spring-boot-starter-security-test únicamente para pruebas, incluyendo
la integración de MockMvc con Spring Security en Boot 4. Las pruebas existentes
del CRUD se ejecutan con un usuario ADMIN simulado, sin desactivar los filtros.
SecurityIntegrationTests usa JWT realmente firmados con JwtEncoder y valida
usuarios, consulta por rol, denegación de modificaciones a USER, eliminación
por ADMIN, tokens vencidos o alterados, roles desconocidos y ausencia de sesión.
Las pruebas se ejecutan con una clave criptográfica temporal en la variable
de entorno, distinta de la clave local de IntelliJ, sin mostrarla ni guardarla.

Verificación final: Maven ejecutó 30 pruebas, todas aprobadas. Para ejecutar
las pruebas desde IntelliJ, configurar también JWT_SECRET_BASE64 en la
configuración de JUnit; la configuración de ejecución de la aplicación
no se hereda automáticamente.

Referencia de integración de pruebas para Boot 4:
https://docs.spring.io/spring-boot/api/java/org/springframework/boot/security/test/autoconfigure/webmvc/SecurityMockMvcAutoConfiguration.html

## Paso 6. DTO, emisión JWT y login

Se implementaron los cinco archivos con el código del docente, adaptando
solamente los paquetes e imports a ni.edu.uam.gestionproductos:

- LoginRequest es un record con username y password.
- LoginResponse es un record con token y type.
- TokenService emite JWT HS256 con issuer nicamarket-laboratorio, usuario en
  subject, fecha de emisión, vencimiento de 30 minutos y roles sin ROLE_.
- POST /api/auth/login autentica las credenciales y devuelve token y type Bearer.
- GET /api/publico/saludo devuelve {"mensaje":"API NicaMarket operativa"}.

Para probar en Postman:

1. Iniciar la configuración GestionProductosApplication JWT en IntelliJ.
2. Enviar GET http://localhost:8080/api/publico/saludo sin autenticación.
3. Enviar POST http://localhost:8080/api/auth/login con Content-Type:
   application/json y cuerpo {"username":"operador","password":"Operador123!"}.
4. Usar el campo token de la respuesta en Authorization → Bearer Token para
   GET http://localhost:8080/api/productos.
5. Repetir el login con admin y su contraseña de laboratorio para operaciones
   de modificación. Una contraseña incorrecta devuelve HTTP 401.

Las credenciales del ejemplo son exclusivamente de desarrollo. No guardar
tokens generados ni el secreto JWT en el repositorio o en capturas.

LoginIntegrationTests verifica el saludo anónimo, login de ambos usuarios,
firma y claims mediante JwtDecoder, duración exacta de 30 minutos, acceso
a productos con el token emitido, restricción de escritura para USER,
ausencia de sesión y rechazo de credenciales incorrectas.

Verificación: suite completa de Maven con 33 pruebas aprobadas, sin fallos
ni errores, usando PostgreSQL y una clave temporal no publicada.

## Paso 7. Verificar en Postman

Se creó e importó en Postman instalado la colección **NicaMarket – JWT**:
postman/nicamarket-jwt.postman_collection.json.
La variable de colección baseUrl contiene http://localhost:8080.

Ejecución real en Collection Runner el 12 de octubre de 2026, 20:01,
hora de Nicaragua: 11 solicitudes, 16 verificaciones aprobadas, cero fallos,
cero errores y cero pruebas omitidas.

| Prueba | Resultado observado |
| --- | --- |
| Saludo sin token | 200 |
| Login ADMIN válido | 200 y token Bearer |
| Login con contraseña incorrecta | 401 |
| Productos sin token | 401 |
| Productos con USER | 200 |
| Crear producto con USER | 403 |
| Productos con firma del token modificada | 401 |
| Crear producto con ADMIN | 200 y producto persistido |

El POST de productos devuelve 200 porque el controlador actual no declara
201 Created. La autorización y la creación fueron correctas.

Las tres solicitudes adicionales preparan el token USER, seleccionan una
categoría existente y eliminan únicamente el producto temporal creado en
esta ejecución. La limpieza respondió 204 para el producto 210.

Para repetir: iniciar la aplicación con su configuración JWT, abrir la colección,
pulsar Run y ejecutar todas las solicitudes en orden con una iteración. Se
requiere al menos una categoría existente. Los cuerpos de los POST incluyen
los campos del ProductoRequestDTO y un código generado para la prueba.

Los tokens se almacenan en variables locales de la ejecución (pm.variables),
no en variables persistentes de la colección. La solicitud final elimina
esas variables. Por ello, para enviar solicitudes individualmente con Send,
copiar manualmente el token del login a Authorization → Bearer Token, como
indica el docente. No publicar ni exportar respuestas que contengan tokens.

Evidencias reales de Postman, sin mostrar claves ni tokens:

- [Saludo, login y acceso anónimo](evidencias-jwt/01-saludo-login-anonimo.png)
- [Consultas y permisos USER](evidencias-jwt/02-user-permisos.png)
- [Firma alterada, ADMIN y limpieza](evidencias-jwt/03-firma-admin-limpieza.png)

Se detuvo la instancia temporal después de las pruebas para liberar 8080.
