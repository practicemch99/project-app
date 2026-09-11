-- Technical smoke-check table; business schema is intentionally undecided.
CREATE TABLE setup_marker (
    id BIGINT NOT NULL PRIMARY KEY,
    description VARCHAR(100) NOT NULL
);
INSERT INTO setup_marker (id, description) VALUES (1, 'Local database initialized');
