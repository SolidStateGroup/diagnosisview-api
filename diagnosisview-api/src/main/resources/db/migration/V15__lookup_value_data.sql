-- new sequence for pv_lookup_values table
CREATE SEQUENCE IF NOT EXISTS
lookup_value_seq
  START 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

-- CHANGE "CURRENT VALUE" OF "SEQUENCE "lookup_value_seq" as we have some records already
ALTER SEQUENCE "public"."lookup_value_seq" RESTART 150;

ALTER TABLE pv_lookup_value ADD COLUMN data jsonb;
