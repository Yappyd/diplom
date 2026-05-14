CREATE DATABASE message_db;
CREATE USER message_user WITH PASSWORD :MESSAGE_PWD;

ALTER DATABASE message_db OWNER TO message_user;

\connect message_db

GRANT USAGE, CREATE ON SCHEMA public TO message_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO message_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO message_user;
