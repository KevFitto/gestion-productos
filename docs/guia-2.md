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

## Paso 8. Completar la relación Uno a Muchos

Se agregó en Categoria el campo List<Producto> productos con
@OneToMany(mappedBy = "categoria"), exactamente como indica el ejemplo, junto
con el import de java.util.List. JPA accede directamente a los campos en estas
entidades, por lo que no requiere getters ni setters para mapear esta colección.

### ¿Cuál entidad contiene realmente la clave foránea?

La tabla producto contiene categoria_id, que referencia categoria.id.
Producto es el lado propietario de la relación mediante @ManyToOne y @JoinColumn.
Categoria es el lado inverso: mappedBy = "categoria" se refiere al atributo
categoria de la clase Producto, no al nombre de la columna SQL.
Esta relación reutiliza la clave foránea existente y no necesita otra migración.

La prueba de integración guarda dos productos de una categoría y consulta
Categoria.productos mediante JPQL para comprobar el mapeo inverso.

El campo nuevo se mantiene privado sin getter, como en el fragmento del profesor,
y no se expone actualmente en JSON. Si un paso posterior lo expone, habrá que
revisar el posible ciclo Categoria → productos → categoria al serializar.

## Paso 9. Consultar productos por categoría

Se agregó findByCategoriaId(Integer categoriaId) en ProductoRepository,
listarPorCategoria en ProductoService y GET /api/productos/categoria/{categoriaId}
en ProductoController, siguiendo los tres fragmentos del profesor.

Spring Data JPA deriva la consulta del nombre del método y filtra por el id de
la categoría relacionada. No es necesario escribir SQL ni agregar una migración.
Si no hay productos asociados al ID recibido, devuelve HTTP 200 con una lista
vacía, incluso si la categoría no existe, tal como resulta del ejemplo.

Las pruebas automatizadas verifican que se obtengan únicamente los productos de
la categoría solicitada y que una categoría sin productos devuelva una lista vacía.
La colección de Postman incluye GET /api/productos/categoria/{{categoriaId}}, con
categoriaId = 1 como en la guía. Reiniciar la aplicación desde IntelliJ antes de
probar y elegir otro ID si se desean consultar los productos de otra categoría.

## Paso 10. Crear la migración para Etiqueta

Se agregó src/main/resources/db/migration/V4__crear_etiquetas.sql con el SQL
exacto de la guía. Crea etiqueta y producto_etiqueta con sus claves foráneas
y una clave primaria compuesta por producto_id y etiqueta_id.

### ¿Por qué una relación Muchos a Muchos requiere una tabla intermedia?

En este modelo relacional, un producto puede tener varias etiquetas y una etiqueta
puede asociarse con varios productos. Una sola clave foránea en cualquiera de las
dos tablas no permite representar ambas multiplicidades.

La tabla intermedia guarda una fila por asociación, con una clave foránea hacia
cada tabla. Por ejemplo, las parejas (1, 2), (1, 3) y (2, 2) indican que el producto
1 tiene las etiquetas 2 y 3, y el producto 2 también tiene la etiqueta 2.
La clave primaria compuesta impide repetir una misma asociación y las claves
foráneas impiden referenciar productos o etiquetas inexistentes.

La entidad Etiqueta y su mapeo JPA se incorporarán cuando lo indique la guía.

Verificación: Flyway aplicó V4 correctamente (success = true). Se consultaron
ambas tablas en PostgreSQL y se confirmaron la restricción UNIQUE de nombre,
la clave primaria compuesta y las dos claves foráneas. Las 14 pruebas existentes
pasaron después de aplicar la migración.

## Paso 11. Crear entidad y repositorio Etiqueta

Se creó Etiqueta con @Entity, @Table(name = "etiqueta"), id Integer generado
con GenerationType.IDENTITY y nombre String, junto con sus getters y setters,
tal como indica el ejemplo. Se creó EtiquetaRepository extendiendo
JpaRepository<Etiqueta, Integer>.

La entidad corresponde a la tabla creada en V4; el repositorio proporciona las
operaciones de persistencia sin implementar manualmente sus métodos.

## Paso 12. Relacionar Producto y Etiqueta

Se agregó en Producto el campo Set<Etiqueta> etiquetas = new HashSet<>() con
@ManyToMany y @JoinTable, tal como indica la guía. Se añadieron los imports de
Set y HashSet.

La relación utiliza producto_etiqueta, creada en V4. joinColumns identifica
producto_id, que referencia al producto propietario de la relación, e
inverseJoinColumns identifica etiqueta_id, que referencia a la etiqueta.
Así, un producto puede tener varias etiquetas y una etiqueta puede estar asociada
con varios productos.

En este paso se agrega únicamente el campo y su mapeo. JPA accede directamente
a los campos; la guía todavía no añade métodos de acceso ni endpoints para
administrar las etiquetas de un producto. No se necesita una nueva migración.

## Paso 13. Crear etiquetas

Se revisó y conservó EtiquetaController implementado por el estudiante. Expone
POST /api/etiquetas, recibe un objeto con nombre, lo guarda mediante
EtiquetaRepository y devuelve la etiqueta con su ID y HTTP 201 Created.

Se verificaron en PostgreSQL las cinco etiquetas que el estudiante ya había creado:

| ID | Nombre |
| --- | --- |
| 1 | Oferta |
| 2 | Importado |
| 3 | Empresarial |
| 4 | Portátil |
| 5 | Gaming |

No se volvieron a insertar. La colección de Postman incluye las cinco solicitudes
para reproducir el paso en una base sin esas etiquetas. Repetir un nombre existente
produce un conflicto porque V4 define nombre como UNIQUE.

La prueba automatizada crea una etiqueta temporal con nombre único, comprueba
HTTP 201 y el ID generado, y consulta la entidad persistida. La transacción de
prueba se revierte al finalizar. Las 15 pruebas de la copia de trabajo pasaron.
Los avances existentes del paso 14 y los retos se conservaron fuera del commit
de este paso para revisarlos cuando corresponda.

## Paso 14. Asociar etiquetas a un producto

Se revisó y conservó la implementación del estudiante: EtiquetaRepository se
inyecta por constructor en ProductoService; agregarEtiqueta busca ambos registros,
añade la etiqueta al Set y guarda el producto, siguiendo el código del profesor.
@Transactional permite la escritura en el servicio configurado como solo lectura.
Se incluyen los getters y setters de etiquetas necesarios para acceder a la colección.

ProductoController expone POST /api/productos/{productoId}/etiquetas/{etiquetaId}
y devuelve el producto actualizado con HTTP 200. No se necesita body.
La colección Postman incluye la solicitud con variables. Para reproducir el ejemplo,
usar productoId=2 y etiquetaId=1, comprobando que existan, y reiniciar desde IntelliJ.

La prueba automatizada asocia una etiqueta con un producto temporal, comprueba
la fila en producto_etiqueta y consulta el producto por GET. También repite la
asociación tras recargar los datos para verificar que no se duplica.
Los cambios de los retos permanecen en la copia de trabajo, fuera de este commit.

## Reto 1. Eliminar asociación Producto–Etiqueta

Se revisó y conservó removerEtiqueta y el endpoint
DELETE /api/productos/{productoId}/etiquetas/{etiquetaId} creados por el estudiante.
El servicio retira del Set únicamente la etiqueta con el ID indicado y guarda
el producto dentro de una transacción. Hibernate elimina la asociación de
producto_etiqueta; no se llama a delete para Producto ni para Etiqueta.

La respuesta es HTTP 200 con el producto actualizado. Repetir la eliminación de
una asociación ausente devuelve el producto sin cambios. Un producto inexistente
devuelve 404 mediante buscarPorId.

Las pruebas verifican que permanezcan el producto, la etiqueta, las demás etiquetas
del producto y las asociaciones de otros productos. También comprueban en SQL que
la pareja eliminada ya no está y que repetir la solicitud no causa error.

## Reto 2. Consultar productos por etiqueta

Se revisó y conservó findByEtiquetasId en ProductoRepository, listarPorEtiqueta
en ProductoService y GET /api/productos/etiqueta/{etiquetaId} en el controlador.
Spring Data JPA deriva la consulta recorriendo la colección etiquetas y su id.
La respuesta es HTTP 200 con la lista de productos asociados; sin coincidencias
devuelve [], incluso si el ID de etiqueta no existe.

Las pruebas usan productos con varias etiquetas y una etiqueta compartida entre
productos. Verifican el filtro sin duplicados, que la consulta refleje una
asociación eliminada y las respuestas vacías. Las 20 pruebas del proyecto pasaron.

Para ambos retos, reiniciar la aplicación desde IntelliJ y configurar productoId
y etiquetaId en la colección de Postman. El DELETE de asociación no lleva body.
Las pruebas automatizadas usan datos temporales y revierten sus cambios.
