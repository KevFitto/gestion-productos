CREATE TABLE proveedor (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    correo VARCHAR(150) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Nullable para conservar los productos creados antes de V3.
ALTER TABLE producto ADD COLUMN proveedor_id INTEGER;

ALTER TABLE producto
ADD CONSTRAINT fk_producto_proveedor
FOREIGN KEY (proveedor_id) REFERENCES proveedor(id);

CREATE INDEX idx_producto_categoria ON producto(categoria_id);
CREATE INDEX idx_producto_proveedor ON producto(proveedor_id);

