# Gestión de productos
Práctica de Servicios Web: Spring Boot, Spring Data JPA, PostgreSQL y Flyway.

## Ejecutar
Requiere Java 21 o posterior y PostgreSQL. La conexión existente se conserva en
`src/main/resources/application.properties`. Si se utiliza otra instalación, ajustar sus credenciales.
Crear la base `gestion_productos` si todavía no existe; Flyway crea las tablas.

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

El puerto predeterminado es 8080. Si está ocupado:
```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=8081'
```

En este equipo, Java 25 necesitó una ruta temporal corta para sus sockets internos.
El comando verificado y actualmente utilizado es:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.port=8080' '-Dspring-boot.run.jvmArguments=-Djdk.net.unixdomain.tmpdir=C:\Windows\Temp'
```

## API
| Recurso | Consultar | Crear |
| --- | --- | --- |
| Categorías | GET /api/categorias | POST /api/categorias |
| Productos | GET /api/productos | POST /api/productos |
| Proveedores | GET /api/proveedores | POST /api/proveedores |

Los POST devuelven 201; datos inválidos, 400; relaciones inexistentes, 404; conflictos de integridad, 409.
Los productos requieren categoría; proveedor es opcional para admitir productos anteriores a V3.
El código del producto es único. Los POST crean registros y descartan IDs de entidad enviados por el cliente.

## Datos y Postman
Importar `postman/gestion-productos.postman_collection.json`.
La variable `baseUrl` está configurada en `http://localhost:8080`.
Ejecutar en orden: tres categorías, dos proveedores, dos productos y consultas.
Los IDs se guardan automáticamente; repetir la colección crea más registros.

También se pueden cargar ejemplos sin duplicar los nombres/códigos existentes:
```powershell
.\scripts\cargar-ejemplos.ps1 -BaseUrl http://localhost:8080
```

El informe está en `docs/entrega.md`. Las pruebas usan PostgreSQL configurado,
aplican migraciones pendientes y revierten las inserciones de prueba (las secuencias pueden avanzar).

