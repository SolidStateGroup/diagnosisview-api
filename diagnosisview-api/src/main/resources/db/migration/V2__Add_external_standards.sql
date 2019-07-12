insert into pv_external_standard (id,description,name) values (1,'ICD-10','ICD-10') ON CONFLICT DO NOTHING;
insert into pv_external_standard (id,description,name) values (2,'ICD-9','ICD-9') ON CONFLICT DO NOTHING;
insert into pv_external_standard (id,description,name) values (3,'SNOMED','SNOMED') ON CONFLICT DO NOTHING;
