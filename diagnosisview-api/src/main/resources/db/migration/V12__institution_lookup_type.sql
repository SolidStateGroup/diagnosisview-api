-- New lookup Type for Institution
INSERT INTO pv_lookup_type(id, creation_date, description, lookup_type, created_by)
VALUES (23, now(), 'Institution Types','INSTITUTION_TYPE', null);

-- Institution Types
INSERT INTO pv_lookup_value(id, creation_date, value, description, display_order, created_by, lookup_type_id)
VALUES (140, now(), 'UNIVERSITY_OF_EDINBURGH','University of Edinburgh',1, null, '23');
INSERT INTO pv_lookup_value(id, creation_date, value, description, display_order, created_by, lookup_type_id)
VALUES (141, now(), 'NHS_SCOTLAND_KNOWLEDGE_NETWORK','NHS Scotland Knowledge Network',2,null, '23');
INSERT INTO pv_lookup_value(id, creation_date, value, description, display_order, created_by, lookup_type_id)
VALUES (142, now(), 'UNIVERSITY_OF_MALAWI','University of Malawi',3,null, '23');
INSERT INTO pv_lookup_value(id, creation_date, value, description, display_order, created_by, lookup_type_id)
VALUES (143, now(), 'OTHER','Other',4,null, '23');
INSERT INTO pv_lookup_value(id, creation_date, value, description, display_order, created_by, lookup_type_id)
VALUES (144, now(), 'NONE','None',5,null, '23');
