CREATE TABLE dv_nhschoices_condition
(
  id                              BIGINT NOT NULL,
  name                            TEXT NOT NULL,
  uri                             TEXT NOT NULL,
  code                            TEXT NOT NULL,
  description                     TEXT,
  description_last_update_date    TIMESTAMP,
  introduction_url                TEXT,
  introduction_url_Status         INT,
  introduction_url_last_update_date    TIMESTAMP,
  created_by                      BIGINT REFERENCES dv_user (id),
  creation_date                   TIMESTAMP NOT NULL,
  last_update_date                TIMESTAMP,
  last_updated_by                 BIGINT REFERENCES dv_user (id),
  PRIMARY KEY (id)
);
