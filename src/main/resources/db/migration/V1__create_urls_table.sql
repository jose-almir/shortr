CREATE TABLE urls (
    id         BIGSERIAL    PRIMARY KEY,
    short_code VARCHAR(20)  UNIQUE,
    long_url   TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_short_code ON urls (short_code);
CREATE INDEX idx_long_url ON urls (long_url);
