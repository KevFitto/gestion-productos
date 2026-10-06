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
