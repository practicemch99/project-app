-- Local development credentials only. Executed only on a new volume.
CREATE USER 'app_core'@'%' IDENTIFIED BY 'local-core-only';
GRANT SELECT, INSERT, UPDATE, DELETE ON appdb.* TO 'app_core'@'%';
CREATE USER 'app_migration'@'%' IDENTIFIED BY 'local-migration-only';
GRANT ALL PRIVILEGES ON appdb.* TO 'app_migration'@'%';
