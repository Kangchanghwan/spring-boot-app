#!/bin/bash
set -e

echo "START INIT-USER-DB";

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  create user testuser with encrypted password 'testpass';
  create database testdb;
  grant all privileges on database testdb to testuser;
  COMMIT;
EOSQL