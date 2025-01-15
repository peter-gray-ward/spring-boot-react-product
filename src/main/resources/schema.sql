CREATE TABLE IF NOT EXISTS "USER" (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS "IMAGE" (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    src BYTEA NOT NULL
);

CREATE TABLE IF NOT EXISTS "PRODUCT" (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(6, 3) NOT NULL,
    description TEXT,
    image_id BIGINT,
    FOREIGN KEY (image_id) REFERENCES "IMAGE"(id)
);


