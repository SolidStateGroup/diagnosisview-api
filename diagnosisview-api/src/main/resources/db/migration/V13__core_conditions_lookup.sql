-- New lookup Type for Tag Types
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)
VALUES (24, now(), 'Tag Types','TAG_TYPES', null);

-- Tag Types
INSERT INTO pv_lookup_value(id, creation_date, value, description, display_order, created_by, lookup_type_id)
VALUES (145, now(), 'CORE_CONDITION','Core Condition',1, null, 24);

ALTER TABLE pv_code ADD COLUMN tags jsonb;
