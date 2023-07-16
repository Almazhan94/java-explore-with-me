DROP table IF EXISTS stat;

CREATE TABLE IF NOT EXISTS stat (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  app VARCHAR(255) NOT NULL,
  uri VARCHAR(512) NOT NULL,
  ip VARCHAR(255) NOT NULL,
  time_stamp timestamp without time zone NOT NULL,
  CONSTRAINT pk_stat PRIMARY KEY (id)
);