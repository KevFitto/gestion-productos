CREATE TABLE categoria (
                           id SERIAL PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL,
                           activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE producto (
                          id SERIAL PRIMARY KEY,
                          codigo VARCHAR(30) NOT NULL UNIQUE,
                          nombre VARCHAR(150) NOT NULL,
                          precio_venta NUMERIC(12,2) NOT NULL,
                          existencia INTEGER NOT NULL DEFAULT 0,
                          categoria_id INTEGER NOT NULL,

                          CONSTRAINT fk_producto_categoria
                              FOREIGN KEY (categoria_id)
                                  REFERENCES categoria(id)
);