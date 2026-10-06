# Práctica guiada 2

## Paso 1. Organizar la estructura del proyecto

Se verificaron controller, entity y repository. Se agregaron dto y service con
archivos package-info.java que documentan su finalidad y permiten conservar los
paquetes en Git antes de incorporar sus clases.

```text
ni.edu.uam.gestionproductos
├── controller
├── dto
├── entity
├── repository
└── service
```

### Responsabilidades

| Paquete | Responsabilidad |
| --- | --- |
| controller | Recibir solicitudes HTTP, validar la entrada, delegar a los servicios y devolver respuestas HTTP. |
| dto | Definir los datos de entrada y salida de la API, separados de las entidades persistentes. |
| entity | Representar los datos persistentes y sus relaciones mediante anotaciones JPA. |
| repository | Consultar y guardar entidades en la base de datos mediante Spring Data JPA. |
| service | Implementar reglas de negocio, coordinar repositorios y definir los límites de las transacciones. |

La arquitectura objetivo es Controller → Service → Repository → PostgreSQL.
En este paso solo se organiza la estructura; los controladores existentes aún
acceden directamente a los repositorios. Los servicios y DTOs se incorporarán
en los siguientes pasos.

## Paso 2. Crear ProductoService

Se creó ProductoService con listar, buscarPorId, guardar y eliminar.
ProductoController ahora delega al servicio y ya no depende de repositorios.
La resolución y validación de categoría y proveedor se trasladó al servicio,
conservando las relaciones y respuestas de error de la práctica anterior.
Las operaciones de escritura se ejecutan en transacciones; las consultas son
de solo lectura. El POST sigue descartando el ID recibido para crear registros;
guardar en el servicio conserva el ID para admitir futuras actualizaciones.
No se agregaron endpoints nuevos en este paso.

### ¿Por qué el Controller no debería utilizar directamente ProductoRepository?

El controlador debe ocuparse de HTTP y delegar las reglas de negocio al servicio.
Así las reglas se centralizan, pueden reutilizarse y probarse independientemente
de los endpoints, y una operación que utiliza varios repositorios puede ejecutarse
dentro de una misma transacción.

### Observaciones sobre el ejemplo

- El bloque está etiquetado como TypeScript, pero el código es Java.
- RuntimeException("Producto no encontrado") no expresa un estado HTTP: sin
  manejo adicional, al llegar a un endpoint produciría un error 500. Se usa
  ResponseStatusException con 404, siguiendo el manejo existente del proyecto.
- Antes de eliminar se comprueba que el producto exista, devolviendo también 404
  si no existe.

Estas observaciones no permiten asegurar cuál era el error anunciado por el profesor.

## Paso 3. Refactorizar ProductoController

ProductoController queda como el ejemplo del profesor: inyección de ProductoService,
GET /api/productos para listar y GET /api/productos/{id} para buscar.
El POST de productos se retira de este controlador siguiendo el código mostrado.
ProductoService conserva el estado del commit del paso 2.

Las pruebas automatizadas se adaptaron a los GET y verifican el listado, la búsqueda
con sus relaciones y la respuesta ante un ID inexistente.

Para Postman, importar postman/guia-2.postman_collection.json y reiniciar la aplicación
desde IntelliJ para cargar el controlador actualizado. Consultar primero el listado
y después un ID existente. La variable productoId comienza en 1 como en la guía,
pero ese registro puede no existir; en ese caso el servicio actual devuelve 404.
La colección de la primera guía conserva sus solicitudes como referencia histórica;
sus POST de productos no están disponibles en este paso.

El bloque de la guía está etiquetado como Kotlin, aunque su contenido es Java.
