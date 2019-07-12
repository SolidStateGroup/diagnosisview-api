ALTER TABLE pv_link ADD COLUMN external_id TEXT;

INSERT INTO pv_lookup_value (id, creation_date, description, display_order, value, dv_only)
VALUES (987654, now(), 'BMJ', 3, 'BMJ', false);