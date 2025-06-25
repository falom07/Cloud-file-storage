--liquibase formated sql

--changeset falom07:1
create table if not exists users(
    id BIGSERIAL PRIMARY KEY,
    username varchar unique not null,
    password varchar not null
)

