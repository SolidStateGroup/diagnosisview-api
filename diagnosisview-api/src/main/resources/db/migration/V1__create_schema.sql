-- CREATE TABLE "user" -------------------------------
CREATE TABLE "public"."user" (
	"id" BIGINT NOT NULL,
	"date_created" Timestamp Without Time Zone,
	"expiry_date" Timestamp Without Time Zone,
	"password" Character Varying( 255 ),
	"salt"     Character Varying( 255 ),
	"projects" JSONB,
	"sectors" JSONB,
	"token" Character Varying( 255 ),
	"username" Character Varying( 255 ),
  "profile_image"     Bytea,
	PRIMARY KEY ( "id" ) );
 ;



CREATE SEQUENCE hibernate_sequence
INCREMENT 1
MINVALUE 1
MAXVALUE 9223372036854775807
START 1
CACHE 1;