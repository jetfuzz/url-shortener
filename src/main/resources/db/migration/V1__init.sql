CREATE TABLE short_urls
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url          TEXT        NOT NULL,
    short_code   VARCHAR(6)  NOT NULL UNIQUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    access_count BIGINT      NOT NULL DEFAULT 0
);