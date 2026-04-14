--liquibase formatted sql

--changeset falom07:2
CREATE TABLE IF NOT EXISTS resources
(
    id         BIGSERIAL PRIMARY KEY,
    path       VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    size       BIGINT,
    type       VARCHAR(20)  NOT NULL CHECK (type IN ('FILE', 'DIRECTORY')),
    owner_id   BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_resources_owner
        FOREIGN KEY (owner_id) REFERENCES users (id)
);