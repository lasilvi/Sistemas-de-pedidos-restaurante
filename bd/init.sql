CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE product (
  id_product UUID PRIMARY KEY,
  product_name VARCHAR(100) NOT NULL UNIQUE,
  product_description VARCHAR(255),
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE orders (
  id_orders UUID PRIMARY KEY,
  table_number INT NOT NULL CHECK (table_number BETWEEN 1 AND 9),
  status_order VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE order_item (
  id_order_item UUID PRIMARY KEY,
  id_orders UUID NOT NULL REFERENCES orders(id),
  id_product UUID NOT NULL REFERENCES product(id),
  quantity INT NOT NULL CHECK (quantity > 0),
  note VARCHAR(255)
);

INSERT INTO product (id, name, description)
VALUES
  (uuid_generate_v4(), 'Hamburguesa', 'Hamburguesa clásica'),
  (uuid_generate_v4(), 'Pizza', 'Pizza personal'),
  (uuid_generate_v4(), 'Gaseosa', 'Bebida fría');
