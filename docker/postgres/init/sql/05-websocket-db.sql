CREATE DATABASE websocket_db;
CREATE USER websocket_user WITH PASSWORD :WEBSOCKET_PWD;

ALTER DATABASE websocket_db OWNER TO websocket_user;

\connect websocket_db

GRANT USAGE, CREATE ON SCHEMA public TO websocket_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO websocket_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO websocket_user;
