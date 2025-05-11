CREATE ROLE mrs LOGIN PASSWORD 'mrs' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;

CREATE DATABASE mrs
 WITH OWNER = mrs
      ENCODING = 'UTF8'
      TABLESPACE = pg_default
      LC_COLLATE = 'C'
      LC_CTYPE = 'C'
      TEMPLATE = template0
      CONNECTION LIMIT = -1;



CREATE TABLE IF NOT EXISTS meeting_room (
  room_id SERIAL,
  room_name VARCHAR(255) NOT NULL,
  PRIMARY KEY(room_id)
);

CREATE TABLE IF NOT EXISTS reservable_room (
  reserved_date DATE,
  room_id INT REFERENCES meeting_room(room_id),
  PRIMARY KEY(reserved_date, room_id)
);

CREATE TABLE IF NOT EXISTS usr (
  user_id VARCHAR(255),
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role_name VARCHAR(255) NOT NULL,
  PRIMARY KEY(user_id)
);

CREATE TABLE IF NOT EXISTS reservation (
  reservation_id SERIAL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  reserved_date DATE NOT NULL,
  room_id INT NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  PRIMARY KEY(reservation_id),
  FOREIGN KEY(reserved_date, room_id) REFERENCES reservable_room(reserved_date, room_id),
  FOREIGN KEY(user_id) REFERENCES usr(user_id)
);
