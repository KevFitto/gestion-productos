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

## Paso 4. Crear ProductoRequestDTO

Se creó ProductoRequestDTO en el paquete dto con los campos codigo y nombre
(String), precioVenta (BigDecimal), existencia y categoriaId (Integer), junto
con sus getters y setters, tal como indica la guía.

### ¿Qué diferencia existe entre una entidad JPA y un DTO?

Una entidad JPA representa datos persistentes y sus relaciones; se mapea a la
base de datos mediante anotaciones como @Entity, @Id y @ManyToOne y es administrada
por JPA. Un DTO transporta los datos que necesita una operación de la API, sin
ser una entidad persistente ni requerir un mapeo a una tabla.

Por ejemplo, Producto tiene una relación con un objeto Categoria, mientras que
ProductoRequestDTO recibe únicamente categoriaId. Esto permite definir la entrada
de la API sin exponer directamente toda la entidad. Su uso en el controlador y
el servicio se incorporará cuando lo indique la guía.

## Paso 5. Registrar productos utilizando el DTO

Se agregó guardar(ProductoRequestDTO dto) con la búsqueda de categoría y el mapeo
de los cinco campos mostrados por el profesor. CategoriaRepository se inyecta
por constructor. Se conserva RuntimeException("Categoría no encontrada") tal como
aparece en el ejemplo. El POST /api/productos recibe el DTO y delega al servicio;
devuelve HTTP 200, que es el comportamiento del controlador indicado en la guía.

El método nuevo mantiene @Transactional para permitir escrituras, ya que el
servicio del paso 2 tiene @Transactional(readOnly = true) a nivel de clase.
El método previo guardar(Producto) se conserva como sobrecarga; el POST ahora
utiliza exclusivamente guardar(ProductoRequestDTO).

### ¿Qué ventaja ofrece enviar categoriaId en lugar del objeto Categoria?

La solicitud es más sencilla y solo identifica una categoría existente. El servidor
la consulta en la base de datos, evitando recibir datos redundantes o contradictorios
como un nombre o estado distinto para la misma categoría. También separa el formato
de entrada de la API de la entidad JPA.

### Prueba

La colección postman/guia-2.postman_collection.json incluye el POST del ejemplo:
codigo TEC-001, nombre Teclado mecánico, precioVenta 75.50, existencia 20 y categoriaId 2.
Reiniciar desde IntelliJ antes de probar. Verificar con GET /api/categorias que
exista la categoría 2; si no existe, utilizar un ID del listado. Para repetir la
creación se debe usar otro código, porque codigo tiene una restricción UNIQUE.

La prueba automatizada crea una categoría temporal, registra el producto con el DTO
y verifica su respuesta y su aparición en el listado con la categoría correspondiente.

## Paso 6. Implementar actualización de productos

Se agregó actualizar(Integer id, ProductoRequestDTO dto) siguiendo el ejemplo:
busca el producto, consulta la categoría, modifica los cinco campos recibidos y
guarda la entidad existente. Se conserva RuntimeException para la categoría no
encontrada. El método lleva @Transactional para habilitar escritura frente a la
configuración de solo lectura del servicio existente.

El controlador expone PUT /api/productos/{id}, recibe el DTO y devuelve el producto
actualizado con HTTP 200. La colección de Postman incluye esta solicitud.

Para probar, reiniciar la aplicación desde IntelliJ, elegir un producto existente
y configurar productoId en Postman. Enviar los cinco campos del DTO con una
categoriaId existente. El código puede conservarse o cambiarse por uno que no
pertenezca a otro producto. Después, consultar el mismo ID mediante GET.

Las pruebas automatizadas verifican que los cambios de campos y categoría se
persisten, el ID se mantiene y no aumenta la cantidad de productos. También
verifican el 404 para un producto inexistente, conforme al buscarPorId que se
conservó del paso 2.

## Paso 7. Implementar eliminación

Se agregó al controlador el método del profesor con @DeleteMapping("/{id}"),
ResponseEntity<Void> y ResponseEntity.noContent().build(). Se reutiliza el método
eliminar del servicio existente. Un producto eliminado devuelve 204 No Content,
sin cuerpo de respuesta. Si no existe, el servicio conservado del paso 2 devuelve 404.

Las pruebas automatizadas eliminan un producto temporal, verifican 204 y cuerpo
vacío, y comprueban que un GET posterior devuelve 404 y que la categoría permanece.
También se verifica la eliminación de un ID inexistente. Los datos de prueba se
revierten mediante transacciones.

En Postman, reiniciar previamente la aplicación desde IntelliJ y ejecutar DELETE
/api/productos/{id} usando el ID de un producto que se quiera eliminar. La colección
incluye la solicitud con la variable productoId; el valor 1 de la guía requiere
que exista ese producto.

| Método | Endpoint | Operación |
| --- | --- | --- |
| GET | /api/productos | Listar |
| GET | /api/productos/{id} | Buscar |
| POST | /api/productos | Crear |
| PUT | /api/productos/{id} | Actualizar |
| DELETE | /api/productos/{id} | Eliminar |
