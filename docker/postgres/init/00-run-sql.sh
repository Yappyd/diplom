#!/bin/sh
set -e

AUTH_PWD="$(cat /run/secrets/DB_AUTH_PASSWORD)"
USER_PWD="$(cat /run/secrets/DB_USER_PASSWORD)"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" \
  -v AUTH_PWD="'$AUTH_PWD'" \
  -v USER_PWD="'$USER_PWD'" \
  -f /docker-entrypoint-initdb.d/sql/01-auth-db.sql \
  -f /docker-entrypoint-initdb.d/sql/02-user-db.sql