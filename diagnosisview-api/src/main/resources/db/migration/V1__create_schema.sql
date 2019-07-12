CREATE SEQUENCE public.hibernate_sequence
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE public.dv_link_logo_rule (
  id character varying(255) NOT NULL,
  link_logo bytea,
  link_logo_filetype character varying(255),
  starts_with character varying(255),
  override_difficulty_level character varying(255)
);

CREATE TABLE public.dv_link_rule_mapping (
  id character varying(255) NOT NULL,
  institution character varying(255),
  replacement_link character varying(255),
  link_id bigint,
  mapping_id character varying(255),
  criteria character varying(255),
  criteria_type character varying(255)
);

CREATE TABLE public.dv_link_rules (
  id character varying(255) NOT NULL,
  institution character varying(255),
  link character varying(255),
  transform character varying(255),
  criteria character varying(255),
  criteria_type character varying(255)
);

CREATE TABLE public.dv_user (
  id bigint NOT NULL,
  date_created timestamp without time zone,
  expiry_date timestamp without time zone,
  password character varying(255),
  payment_data jsonb,
  profile_image bytea,
  profile_image_type character varying(255),
  salt character varying(255),
  token character varying(255),
  username character varying(255),
  favourites jsonb,
  history jsonb,
  first_name character varying(255),
  institution character varying(255),
  last_name character varying(255),
  profession character varying(255),
  email_address character varying(255),
  occupation character varying(255),
  role_type character varying(255),
  active_subscription boolean,
  deleted boolean,
  auto_renewing boolean,
  reset_code character varying(255),
  reset_expiry_date timestamp without time zone
);

CREATE TABLE public.pv_category (
  id bigint NOT NULL,
  friendly_description character varying(255) NOT NULL,
  hidden boolean NOT NULL,
  icd10_description character varying(255) NOT NULL,
  number integer NOT NULL
);

CREATE TABLE public.pv_code (
  id bigint NOT NULL,
  creation_date timestamp without time zone,
  last_update_date timestamp without time zone,
  code character varying(255),
  description character varying(500),
  display_order integer,
  full_description character varying(500),
  hide_from_patients boolean,
  patient_friendly_name character varying(255),
  removed_externally boolean,
  source_type character varying(255) NOT NULL,
  created_by bigint,
  last_updated_by bigint,
  type_id bigint,
  standard_type_id bigint
);

CREATE TABLE public.pv_code_category (
  id bigint NOT NULL,
  category_id bigint NOT NULL,
  code_id bigint NOT NULL
);

CREATE TABLE public.pv_code_external_standard (
  id bigint NOT NULL,
  code character varying(255),
  code_id bigint NOT NULL,
  external_standard_id bigint NOT NULL
);

CREATE TABLE public.pv_external_standard (
  id bigint NOT NULL,
  description character varying(255),
  name character varying(255)
);

CREATE TABLE public.pv_link (
  id bigint NOT NULL,
  creation_date timestamp without time zone,
  last_update_date timestamp without time zone,
  difficulty_level character varying(255),
  display_order integer NOT NULL,
  link character varying(255),
  name character varying(255),
  created_by bigint,
  last_updated_by bigint,
  code_id bigint,
  type_id bigint,
  free_link boolean,
  transformations_only boolean,
  link_logo_id character varying(255)
);

CREATE TABLE public.pv_lookup_type (
  id bigint NOT NULL,
  creation_date timestamp without time zone,
  last_update_date timestamp without time zone,
  description character varying(255),
  lookup_type character varying(255),
  created_by bigint,
  last_updated_by bigint
);

CREATE TABLE public.pv_lookup_value (
  id bigint NOT NULL,
  creation_date timestamp without time zone,
  last_update_date timestamp without time zone,
  description character varying(255),
  description_friendly character varying(255),
  display_order bigint,
  value character varying(255),
  created_by bigint,
  last_updated_by bigint,
  lookup_type_id bigint,
  dv_only boolean
);

ALTER TABLE ONLY public.dv_link_rule_mapping
  ADD CONSTRAINT dv_link_rule_mapping_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.dv_link_logo_rule
  ADD CONSTRAINT dv_link_logo_rule_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.dv_link_rules
  ADD CONSTRAINT dv_link_rules_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.dv_user
  ADD CONSTRAINT dv_user_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pv_category
  ADD CONSTRAINT pv_category_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pv_code_category
  ADD CONSTRAINT pv_code_category_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pv_code_external_standard
  ADD CONSTRAINT pv_code_external_standard_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pv_code
  ADD CONSTRAINT pv_code_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pv_external_standard
  ADD CONSTRAINT pv_external_standard_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pv_link
  ADD CONSTRAINT pv_link_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pv_lookup_type
  ADD CONSTRAINT pv_lookup_type_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pv_lookup_value
  ADD CONSTRAINT pv_lookup_value_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pv_link
  ADD CONSTRAINT fk1va08uv4qpskk67qjey157uwj FOREIGN KEY (code_id) REFERENCES public.pv_code(id);

ALTER TABLE ONLY public.pv_code
  ADD CONSTRAINT fk2u3qvyxu7ctjj9yyhhm0qyi0x FOREIGN KEY (created_by) REFERENCES public.dv_user(id);

ALTER TABLE ONLY public.pv_lookup_value
  ADD CONSTRAINT fk3ifh0tyul0p5t96j6ccc3b75y FOREIGN KEY (lookup_type_id) REFERENCES public.pv_lookup_type(id);

ALTER TABLE ONLY public.pv_code_external_standard
  ADD CONSTRAINT fk54653wnecjpsohjmgf3tg8je9 FOREIGN KEY (code_id) REFERENCES public.pv_code(id);

ALTER TABLE ONLY public.pv_code_external_standard
  ADD CONSTRAINT fk5ngie79y1ite6kqh212j9x6ap FOREIGN KEY (external_standard_id) REFERENCES public.pv_external_standard(id);

ALTER TABLE ONLY public.pv_lookup_type
  ADD CONSTRAINT fk5wojbfyfvdplssy1v1knp7gf4 FOREIGN KEY (created_by) REFERENCES public.dv_user(id);

ALTER TABLE ONLY public.dv_link_rule_mapping
  ADD CONSTRAINT fkbtkdn5f8656gr17x2550pqga8 FOREIGN KEY (id) REFERENCES public.dv_link_rule_mapping(id);

ALTER TABLE ONLY public.pv_code_category
  ADD CONSTRAINT fkc8xt08qpfxx9lbexr6hpjarj9 FOREIGN KEY (code_id) REFERENCES public.pv_code(id);

ALTER TABLE ONLY public.pv_link
  ADD CONSTRAINT fkev2eaeg9urk35v1tiwc7ipi5n FOREIGN KEY (created_by) REFERENCES public.dv_user(id);

ALTER TABLE ONLY public.pv_code
  ADD CONSTRAINT fkgrn4k9rll5xffvyxdh349akel FOREIGN KEY (last_updated_by) REFERENCES public.dv_user(id);

ALTER TABLE ONLY public.pv_link
  ADD CONSTRAINT fkhdj5xsn4hs4ilen1hfp4pdd75 FOREIGN KEY (last_updated_by) REFERENCES public.dv_user(id);

ALTER TABLE ONLY public.pv_code_category
  ADD CONSTRAINT fkien8mus3r2jv5dsl492k82po2 FOREIGN KEY (category_id) REFERENCES public.pv_category(id);

ALTER TABLE ONLY public.pv_code
  ADD CONSTRAINT fkl8ka3vqod2vq6ytgd1er4x91m FOREIGN KEY (standard_type_id) REFERENCES public.pv_lookup_value(id);

ALTER TABLE ONLY public.pv_lookup_value
  ADD CONSTRAINT fkmhh511qx6jegmwkfj0c7c0o1q FOREIGN KEY (created_by) REFERENCES public.dv_user(id);

ALTER TABLE ONLY public.pv_lookup_value
  ADD CONSTRAINT fknw9435djqk55dpma4rdh8kr9d FOREIGN KEY (last_updated_by) REFERENCES public.dv_user(id);

ALTER TABLE ONLY public.pv_lookup_type
  ADD CONSTRAINT fkod144x68hod0174h2xqso2xr FOREIGN KEY (last_updated_by) REFERENCES public.dv_user(id);

ALTER TABLE ONLY public.pv_code
  ADD CONSTRAINT fkoiwqovn1u56lybvaodvicotcd FOREIGN KEY (type_id) REFERENCES public.pv_lookup_value(id);

ALTER TABLE ONLY public.pv_link
  ADD CONSTRAINT fkso0awpnao3k0ina7rafsvb94 FOREIGN KEY (type_id) REFERENCES public.pv_lookup_value(id);

ALTER TABLE ONLY public.dv_link_rule_mapping
  ADD CONSTRAINT fktdw6un6wfv8onf2hdcrcse0t6 FOREIGN KEY (mapping_id) REFERENCES public.dv_link_rules(id);

ALTER TABLE ONLY public.pv_link
  ADD CONSTRAINT fktj92pm6iomc53n6d20argk4e8 FOREIGN KEY (link_logo_id) REFERENCES public.dv_link_logo_rule(id);

ALTER TABLE ONLY public.dv_link_rule_mapping
  ADD CONSTRAINT fkuvw37f6f01f604dmiwebxubj FOREIGN KEY (link_id) REFERENCES public.pv_link(id);
