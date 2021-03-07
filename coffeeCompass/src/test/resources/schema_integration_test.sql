CREATE SCHEMA IF NOT EXISTS COFFEECOMPASS;

CREATE ROLE coffeecompass_dev_user WITH PASSWORD '53normaleuser_passwd.,78' LOGIN;
CREATE ROLE coffeecompass_prod_user WITH PASSWORD '53normaleuser_passwd.,78' LOGIN;

	
/* SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0; */
/* SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0; */
/* SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES'; */

-- -----------------------------------------------------
-- Schema coffeecompass
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS coffeecompass;



-- -----------------------------------------------------
-- PRIVILEGES to new ROLES na only CRUD - tj. SELECT, INSERT, UPDATE, DELETE
-- -----------------------------------------------------

GRANT CONNECT ON DATABASE coffeecompass TO coffeecompass_dev_user;
GRANT SELECT ON ALL TABLES IN SCHEMA coffeecompass TO coffeecompass_dev_user;
GRANT INSERT ON ALL TABLES IN SCHEMA coffeecompass TO coffeecompass_dev_user;
GRANT UPDATE ON ALL TABLES IN SCHEMA coffeecompass TO coffeecompass_dev_user;
GRANT DELETE ON ALL TABLES IN SCHEMA coffeecompass TO coffeecompass_dev_user;
GRANT USAGE ON SCHEMA coffeecompass TO coffeecompass_dev_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA coffeecompass TO coffeecompass_dev_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA coffeecompass TO coffeecompass_dev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA coffeecompass GRANT SELECT ON TABLES TO coffeecompass_dev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA coffeecompass GRANT INSERT ON TABLES TO coffeecompass_dev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA coffeecompass GRANT UPDATE ON TABLES TO coffeecompass_dev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA coffeecompass GRANT DELETE ON TABLES TO coffeecompass_dev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA coffeecompass GRANT EXECUTE ON FUNCTIONS TO coffeecompass_dev_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA coffeecompass GRANT USAGE, SELECT ON SEQUENCES TO coffeecompass_dev_user;

-- REVOKE ALL ON DATABASE coffeecompass FROM coffeecompass_prod_user;
GRANT CONNECT ON DATABASE coffeecompass TO coffeecompass_prod_user;
GRANT SELECT ON ALL TABLES IN SCHEMA coffeecompass TO coffeecompass_prod_user;
GRANT INSERT ON ALL TABLES IN SCHEMA coffeecompass TO coffeecompass_prod_user;
GRANT UPDATE ON ALL TABLES IN SCHEMA coffeecompass TO coffeecompass_prod_user;
GRANT DELETE ON ALL TABLES IN SCHEMA coffeecompass TO coffeecompass_prod_user;
GRANT USAGE ON SCHEMA coffeecompass TO coffeecompass_prod_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA coffeecompass TO coffeecompass_prod_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA coffeecompass TO coffeecompass_prod_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA coffeecompass GRANT SELECT ON TABLES TO coffeecompass_prod_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA coffeecompass GRANT INSERT ON TABLES TO coffeecompass_prod_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA coffeecompass GRANT UPDATE ON TABLES TO coffeecompass_prod_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA coffeecompass GRANT DELETE ON TABLES TO coffeecompass_prod_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA coffeecompass GRANT EXECUTE ON FUNCTIONS TO coffeecompass_prod_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA coffeecompass GRANT USAGE, SELECT ON SEQUENCES TO coffeecompass_prod_user;


-- -----------------------------------------------------	
-- TABLES
-- -----------------------------------------------------


-- -----------------------------------------------------
-- Table `coffeecompass`.`userprofile`
-- -----------------------------------------------------
-- CREATE SEQUENCE coffeecompass.userprofile_seq;
-- DROP TABLE coffeecompass.user_profile;

CREATE TABLE coffeecompass.user_profile
(
    id serial NOT NULL,
    type character varying(30) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT userprofile_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.user_profile
    OWNER to postgres;


-- -----------------------------------------------------
-- Table `coffeecompass`.`user`
-- -----------------------------------------------------
-- DROP TABLE coffeecompass."user";

CREATE TABLE coffeecompass."user"
(
    id serial NOT NULL,
    username character varying(30) COLLATE pg_catalog."default" NOT NULL,
    passwd character varying(80) COLLATE pg_catalog."default",
    first_name character varying(30) COLLATE pg_catalog."default",
    last_name character varying(50) COLLATE pg_catalog."default",
    email character varying(64) COLLATE pg_catalog."default",
    created_on timestamp(0) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    created_sites integer DEFAULT 0,
    updated_sites integer DEFAULT 0,
    deleted_sites integer DEFAULT 0,
    banned boolean NOT NULL DEFAULT false,
    registration_email_confirmed boolean NOT NULL DEFAULT false,
    auth_provider_id smallint NOT NULL DEFAULT 0,
    enabled boolean,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT sso_unique UNIQUE (username)

)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass."user"
    OWNER to postgres;


-- -----------------------------------------------------
-- Table: coffeecompass.user_to_user_profile
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.user_to_user_profile;

CREATE TABLE coffeecompass.user_to_user_profile
(
    user_id bigint NOT NULL,
    user_profile_id bigint NOT NULL,
    CONSTRAINT usertouserprofile_pkey PRIMARY KEY (user_id, user_profile_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id)
        REFERENCES coffeecompass."user" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT fk_user_profile FOREIGN KEY (user_profile_id)
        REFERENCES coffeecompass.user_profile (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.user_to_user_profile
    OWNER to postgres;
COMMENT ON TABLE coffeecompass.user_to_user_profile
    IS 'Spojovací tab. mezi User a user_profile. User může mít více profilů.';




-- -----------------------------------------------------
-- Table `coffeecompass`.`dalsi_automat_vedle_type`
-- -----------------------------------------------------

-- DROP TABLE coffeecompass.dalsi_automat_vedle_type;

CREATE TABLE coffeecompass.dalsi_automat_vedle_type
(
    id serial NOT NULL,
    druh_automatu character varying(45) COLLATE pg_catalog."default" NOT NULL DEFAULT NULL::character varying,
    CONSTRAINT dalsiautomatvedle_pkey PRIMARY KEY (id),
    CONSTRAINT typ_automatu UNIQUE (druh_automatu)

)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.dalsi_automat_vedle_type
    OWNER to postgres;


-- -----------------------------------------------------
-- Table `coffeecompass`.`dodavatel_nebo_jmeno_podniku`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS coffeecompass.dodavatel_nebo_jmeno_podniku (
    id serial NOT NULL,
    jmeno_podniku_dodavatele character varying(85) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT dodavateneboljmenopodniku_pkey PRIMARY KEY (id),
    CONSTRAINT comp_name UNIQUE (jmeno_podniku_dodavatele)
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.dodavatel_nebo_jmeno_podniku
    OWNER to postgres;


-- -----------------------------------------------------
-- Table `coffeecompass`.`druhy_kavy`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS coffeecompass.druhy_kavy (
  id serial NOT NULL,
  druh_kavy character varying(45) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT druhykavy_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.druhy_kavy
    OWNER to postgres;


-- -----------------------------------------------------
-- Table `coffeecompass`.`price_range`
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.price_range;

CREATE TABLE IF NOT EXISTS coffeecompass.price_range (
  id serial NOT NULL,
  price_range character varying(45) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT pricerange_pkey PRIMARY KEY (id),
    CONSTRAINT price UNIQUE (price_range)

)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;


ALTER TABLE coffeecompass.price_range
    OWNER to postgres;
COMMENT ON TABLE coffeecompass.price_range
    IS 'Několik stupňů rozsahů cen.';


-- -----------------------------------------------------
-- Table `coffeecompass`.`nabidka`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS coffeecompass.nabidka (
  id serial NOT NULL,
  nabidka character varying(45) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT nabidka_pkey PRIMARY KEY (id),
    CONSTRAINT unique_nabidka UNIQUE (nabidka)

)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.nabidka
    OWNER to postgres;
COMMENT ON TABLE coffeecompass.nabidka
    IS 'id, typ napoje (kafe, cokolada, caj, polevka)';
	
		

-- -----------------------------------------------------
-- Table: coffeecompass.stars_hodnoceni_kvality
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.stars_hodnoceni_kvality;

CREATE TABLE coffeecompass.stars_hodnoceni_kvality
(
    pocet_hvezdicek integer NOT NULL,
    slovni_vyjadreni_hvezdicek character varying(45) COLLATE pg_catalog."default" NOT NULL DEFAULT NULL::character varying,
    CONSTRAINT slovnihodnocenikvality_pkey PRIMARY KEY (pocet_hvezdicek),
    CONSTRAINT kvalita_unique UNIQUE (slovni_vyjadreni_hvezdicek)
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.stars_hodnoceni_kvality
    OWNER to postgres;


-- -----------------------------------------------------
-- Table: coffeecompass.coffee_site_status
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.coffee_site_status;

CREATE TABLE coffeecompass.coffee_site_status
(
    id serial NOT NULL,
    status_podniku character varying(35) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT statuspodniku_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.coffee_site_status
    OWNER to postgres;


-- -----------------------------------------------------
-- Table: coffeecompass.status_coffee_site_zaznamu
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.status_coffee_site_zaznamu;

CREATE TABLE coffeecompass.status_coffee_site_zaznamu
(
    id serial NOT NULL,
    status_zaznamu character varying(45) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT statuszaznamu_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.status_coffee_site_zaznamu
    OWNER to postgres;
COMMENT ON TABLE coffeecompass.status_coffee_site_zaznamu
    IS 'Status zaznamu v hlavni tab. coffee_site';


-- -----------------------------------------------------
-- Table: coffeecompass.typ_kelimku
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.typ_kelimku;

CREATE TABLE coffeecompass.typ_kelimku
(
    id serial NOT NULL,
    typ_kelimku character varying(45) COLLATE pg_catalog."default" NOT NULL DEFAULT NULL::character varying,
    CONSTRAINT typkelimku_pkey PRIMARY KEY (id),
    CONSTRAINT unique_cup_type UNIQUE (typ_kelimku)
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.typ_kelimku
    OWNER to postgres;


-- -----------------------------------------------------
-- Table: coffeecompass.typ_lokality
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.typ_lokality;

CREATE TABLE IF NOT EXISTS coffeecompass.typ_lokality (
  id serial NOT NULL,
  lokalita character varying(55) COLLATE pg_catalog."default" NOT NULL DEFAULT NULL::character varying,
    CONSTRAINT typlokality_pkey PRIMARY KEY (id),
    CONSTRAINT lokalita_type_unique UNIQUE (lokalita)

)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.typ_lokality
    OWNER to postgres;


-- -----------------------------------------------------
-- Table `coffeecompass`.`typ_podniku`
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.typ_podniku;

CREATE TABLE IF NOT EXISTS coffeecompass.typ_podniku (
  id serial NOT NULL,
  typ_zarizeni character varying(45) COLLATE pg_catalog."default" NOT NULL DEFAULT NULL::character varying,
  CONSTRAINT typpodniku_pkey PRIMARY KEY (id),
  CONSTRAINT coffee_site_type_unique UNIQUE (typ_zarizeni)
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.typ_podniku
    OWNER to postgres;


-- -----------------------------------------------------
-- Table `coffeecompass`.`coffee_site`
-- -----------------------------------------------------

--CREATE SEQUENCE coffeecompass.maincoffeelisttable_seq;

DROP TABLE coffeecompass.coffee_site;

CREATE TABLE IF NOT EXISTS coffeecompass.coffee_site (
  id serial NOT NULL,
  created_on timestamp(0) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    canceled_on timestamp(0) without time zone,
    status_zaznamu_id bigint NOT NULL DEFAULT '4'::bigint,
    zadal_user_id bigint NOT NULL DEFAULT '1'::bigint,
    naposledy_upravil_user_id bigint,
    smazal_user_id bigint,
    typ_podniku_id bigint,
    status_zarizeni_id bigint,
    dodavatel_jmeno_podniku_id bigint,
    hodnoceni_kavy_stars bigint DEFAULT 3,
    cena_id bigint,
    poloha_gps_delka double precision,
    poloha_gps_sirka double precision,
    poloha_mesto character varying(60) COLLATE pg_catalog."default" DEFAULT NULL::character varying,
    poloha_ulice_a_cp character varying(60) COLLATE pg_catalog."default" DEFAULT NULL::character varying,
    casova_pristupnost_dny character varying(50) COLLATE pg_catalog."default" DEFAULT NULL::character varying,
    casova_pristupnost_hodiny character varying(50) COLLATE pg_catalog."default" DEFAULT NULL::character varying,
    komentar_autora character varying(240) COLLATE pg_catalog."default" DEFAULT NULL::character varying,
    pocet_kavovych_automatu_vedle_sebe integer,
    typ_lokality_id bigint,
    site_name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    create_activate_notification_sent boolean DEFAULT true,
    CONSTRAINT maincoffeelisttable_pkey PRIMARY KEY (id),
    CONSTRAINT fk_cena FOREIGN KEY (cena_id)
        REFERENCES coffeecompass.price_range (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT fk_dodavatel FOREIGN KEY (dodavatel_jmeno_podniku_id)
        REFERENCES coffeecompass.dodavatel_nebo_jmeno_podniku (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT fk_hodnoceni_kavy FOREIGN KEY (hodnoceni_kavy_stars)
        REFERENCES coffeecompass.stars_hodnoceni_kvality (pocet_hvezdicek) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT fk_naposledy_editoval_user FOREIGN KEY (naposledy_upravil_user_id)
        REFERENCES coffeecompass."user" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT fk_smazal_user FOREIGN KEY (smazal_user_id)
        REFERENCES coffeecompass."user" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT fk_status_zarizeni FOREIGN KEY (status_zarizeni_id)
        REFERENCES coffeecompass.coffee_site_status (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT fk_status_zaznamu FOREIGN KEY (status_zaznamu_id)
        REFERENCES coffeecompass.status_coffee_site_zaznamu (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT fk_typ_lokality FOREIGN KEY (typ_lokality_id)
        REFERENCES coffeecompass.typ_lokality (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT fk_typ_podniku FOREIGN KEY (typ_podniku_id)
        REFERENCES coffeecompass.typ_podniku (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT fk_vytvoril_user FOREIGN KEY (zadal_user_id)
        REFERENCES coffeecompass."user" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.coffee_site
    OWNER to postgres;
COMMENT ON TABLE coffeecompass.coffee_site
    IS 'Hlavní tab. obsahující informace o jednom "situ"/místě, na kterém je automat nebo jiné zař., kde se dá získat káva.';

COMMENT ON COLUMN coffeecompass.coffee_site.hodnoceni_kavy_stars
    IS 'číslo od 1 - 5 vyjadřující hodnocení, 1 nejhorší. Lze následné použít dotaz (s JOIN?) do tab. slovniHodnoceniKvality';

COMMENT ON COLUMN coffeecompass.coffee_site.cena_id
    IS 'odkaz na zaznam v tab. priceRange';

COMMENT ON COLUMN coffeecompass.coffee_site.poloha_gps_delka
    IS 'Zemepisna delka polohy zarizeni. Typ nastaven na Float, protože to používá Google v jejich GoogleMapAPI';

COMMENT ON COLUMN coffeecompass.coffee_site.poloha_gps_sirka
    IS 'Zeměpisná šířka polohy zařízení.nnTyp nastaven na Float, protože to používá Google v jejich GoogleMapAPI, které využijeme pro zobrazení v mapě';

COMMENT ON COLUMN coffeecompass.coffee_site.poloha_mesto
    IS 'Jméno města ve kterém je zař.';

COMMENT ON COLUMN coffeecompass.coffee_site.poloha_ulice_a_cp
    IS 'Jméno ulice a číslo popisné budovy, kde se zařízení ve městě nachází';

COMMENT ON COLUMN coffeecompass.coffee_site.casova_pristupnost_dny
    IS 'otevírací doba nebo časová přístupnost objektu, kde se zařízení nachází';

COMMENT ON COLUMN coffeecompass.coffee_site.komentar_autora
    IS 'Prostor pro komentář';

COMMENT ON COLUMN coffeecompass.coffee_site.pocet_kavovych_automatu_vedle_sebe
    IS 'Pro situaci, kdy na jednom mistě bývá více jak jeden automat od jednoho dodavatele. Obvykle mají téměř stejnou nabídku a bylo by zbytečné dělat další záznam';

COMMENT ON CONSTRAINT fk_cena ON coffeecompass.coffee_site
    IS 'Odkaz do tab. price_range';
COMMENT ON CONSTRAINT fk_dodavatel ON coffeecompass.coffee_site
    IS 'odkaz do tab. dodavatel_nebo_jmeno_podniku';
COMMENT ON CONSTRAINT fk_hodnoceni_kavy ON coffeecompass.coffee_site
    IS 'Odkaz do tab. slovni_hodnoceni_kvality';
COMMENT ON CONSTRAINT fk_naposledy_editoval_user ON coffeecompass.coffee_site
    IS 'odkaz do tab. user na uživatele, který daný záznam naposledy editoval.';
COMMENT ON CONSTRAINT fk_smazal_user ON coffeecompass.coffee_site
    IS 'Odkaz do tab. user na uživatele, který tento záznam v tab. přesunul do statusu canceled';
COMMENT ON CONSTRAINT fk_status_zarizeni ON coffeecompass.coffee_site
    IS 'Odkaz do tab. status_podniku. Melo by být něco jako, otevřeno, zavřeno, zrušeno, dočasné apod.';
COMMENT ON CONSTRAINT fk_status_zaznamu ON coffeecompass.coffee_site
    IS 'odkaz do tab. status_zaznamu. mělo by být něco jako valid, unknown, invalid (obsahuje chyby) apod.';
COMMENT ON CONSTRAINT fk_typ_lokality ON coffeecompass.coffee_site
    IS 'Odkaz do tab. typ_lokality';
COMMENT ON CONSTRAINT fk_typ_podniku ON coffeecompass.coffee_site
    IS 'Odkaz do tab. typ_podniku';
COMMENT ON CONSTRAINT fk_vytvoril_user ON coffeecompass.coffee_site
    IS 'odkaz do tab. user na uzivatele, který daný záznam v této tab. vytvořil.';


-- -----------------------------------------------------
-- Table: coffeecompass.hodnoceni
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.hodnoceni;

CREATE TABLE coffeecompass.hodnoceni
(
    id serial NOT NULL,
    
    user_id bigint,
    coffeesite_id bigint,
    stars_id bigint,
    CONSTRAINT id_pkey PRIMARY KEY (id),
    CONSTRAINT hodnoceni_user_id_coffeesite_id_stars_id_key UNIQUE (user_id, coffeesite_id, stars_id),
    CONSTRAINT fk_coffeesite FOREIGN KEY (coffeesite_id)
        REFERENCES coffeecompass.coffee_site (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID,
    CONSTRAINT fk_stars FOREIGN KEY (stars_id)
        REFERENCES coffeecompass.stars_hodnoceni_kvality (pocet_hvezdicek) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID,
    CONSTRAINT fk_user FOREIGN KEY (user_id)
        REFERENCES coffeecompass."user" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.hodnoceni
    OWNER to postgres;


-- **********************************************************************************
-- SPOJOVACI TABULKY pro COFFEE_SITE tabulky
-- **********************************************************************************

-- -----------------------------------------------------
-- Table: coffeecompass.coffee_site_to_dalsi_automat_vedle
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.coffee_site_to_dalsi_automat_vedle;

CREATE TABLE coffeecompass.coffee_site_to_dalsi_automat_vedle
(
    id_mainsitetab bigint NOT NULL,
    id_dalsi_automat bigint NOT NULL,
    CONSTRAINT coffee_site_to_dalsi_automat_vedle_pkey PRIMARY KEY (id_mainsitetab, id_dalsi_automat),
    CONSTRAINT fk_dalsi_automat_type FOREIGN KEY (id_dalsi_automat)
        REFERENCES coffeecompass.dalsi_automat_vedle_type (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT fk_main_coffee_list FOREIGN KEY (id_mainsitetab)
        REFERENCES coffeecompass.coffee_site (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.coffee_site_to_dalsi_automat_vedle
    OWNER to postgres;
COMMENT ON TABLE coffeecompass.coffee_site_to_dalsi_automat_vedle
    IS 'Spojovací tab. mezi záznamem v hlavni tab. (coffee_site) a tab. dalsi_automat_vedle, která obsahuje možné druhy automatů, které mohou sousedit s kávovým automatem.';

	
-- -----------------------------------------------------	
-- Table: coffeecompass.coffee_site_to_druhy_kavy
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.coffee_site_to_druhy_kavy;

CREATE TABLE coffeecompass.coffee_site_to_druhy_kavy
(
    coffee_site_id bigint NOT NULL,
    druhy_kavy_id bigint NOT NULL,
    CONSTRAINT coffee_site_to_druhy_kavy_pkey PRIMARY KEY (coffee_site_id, druhy_kavy_id),
    CONSTRAINT fk_coffee_site FOREIGN KEY (coffee_site_id)
        REFERENCES coffeecompass.coffee_site (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID,
    CONSTRAINT fk_druhy_kavy FOREIGN KEY (druhy_kavy_id)
        REFERENCES coffeecompass.druhy_kavy (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.coffee_site_to_druhy_kavy
    OWNER to postgres;	
	
	
-- -----------------------------------------------------	
-- Table: coffeecompass.coffee_site_to_nabidka
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.coffee_site_to_nabidka;

CREATE TABLE coffeecompass.coffee_site_to_nabidka
(
    id_mainsitetab bigint NOT NULL,
    id_nabidka bigint NOT NULL,
    CONSTRAINT coffee_site_to_nabidka_pkey PRIMARY KEY (id_mainsitetab, id_nabidka),
    CONSTRAINT fk_id_mainsitetab FOREIGN KEY (id_mainsitetab)
        REFERENCES coffeecompass.coffee_site (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID,
    CONSTRAINT fk_id_nabidka FOREIGN KEY (id_nabidka)
        REFERENCES coffeecompass.nabidka (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.coffee_site_to_nabidka
    OWNER to postgres;
COMMENT ON TABLE coffeecompass.coffee_site_to_nabidka
    IS 'spojovaci tab. mezi zaznamem v hlavni tab. coffee_site a zaznamem v tab. nabidka';	
	

-- -----------------------------------------------------
-- Table: coffeecompass.coffee_site_to_typ_kelimku
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.coffee_site_to_typ_kelimku;

CREATE TABLE coffeecompass.coffee_site_to_typ_kelimku
(
    coffee_site_id bigint NOT NULL,
    typ_kelimku_id bigint NOT NULL,
    CONSTRAINT coffee_site_to_typ_kelimku_pkey PRIMARY KEY (coffee_site_id, typ_kelimku_id),
    CONSTRAINT fk_coffee_site FOREIGN KEY (coffee_site_id)
        REFERENCES coffeecompass.coffee_site (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID,
    CONSTRAINT fk_typ_kelimku FOREIGN KEY (typ_kelimku_id)
        REFERENCES coffeecompass.typ_kelimku (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.coffee_site_to_typ_kelimku
    OWNER to postgres;	




-- -----------------------------------------------------
-- Table: coffeecompass.persistent_logins
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.persistent_logins;

CREATE TABLE coffeecompass.persistent_logins
(
    username character varying(64) COLLATE pg_catalog."default" NOT NULL,
    series character varying(64) COLLATE pg_catalog."default" NOT NULL,
    token character varying(64) COLLATE pg_catalog."default" NOT NULL,
    last_used timestamp(0) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT persistentlogins_pkey PRIMARY KEY (series)
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.persistent_logins
    OWNER to postgres;
COMMENT ON TABLE coffeecompass.persistent_logins
    IS 'údaje o zalogovaných uživatelých. z projektu Spring MVC Security';



-- -----------------------------------------------------
-- Table: coffeecompass.user_verification_token
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.user_verification_token;

CREATE TABLE coffeecompass.user_verification_token
(
    id serial NOT NULL,
    user_id bigint,
    token character varying(512) COLLATE pg_catalog."default" NOT NULL,
    expiry_date timestamp without time zone,
    CONSTRAINT user_verification_token_pkey PRIMARY KEY (id),
    CONSTRAINT fk_user FOREIGN KEY (user_id)
        REFERENCES coffeecompass."user" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.user_verification_token
    OWNER to postgres;
COMMENT ON TABLE coffeecompass.user_verification_token
    IS 'Unikatni "token", retezec pro verifikaci e-mailu noveho uzivatele pomoci e-mailu.';


-- -----------------------------------------------------
-- Table: coffeecompass.comment
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.comment;

CREATE TABLE coffeecompass.comment
(
    id serial NOT NULL,
    text character varying(512) COLLATE pg_catalog."default" NOT NULL,
    created timestamp with time zone NOT NULL,
    site_id integer NOT NULL,
    user_id integer NOT NULL,
    CONSTRAINT comment_pkey PRIMARY KEY (id),
    CONSTRAINT comment_site_id_fkey FOREIGN KEY (site_id)
        REFERENCES coffeecompass.coffee_site (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID,
    CONSTRAINT comment_user_id_fkey FOREIGN KEY (user_id)
        REFERENCES coffeecompass."user" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.comment
    OWNER to postgres;
	
	
-- -----------------------------------------------------
-- Table: coffeecompass.auth_providers
-- -----------------------------------------------------	
-- DROP TABLE coffeecompass.auth_providers;

CREATE TABLE coffeecompass.auth_providers
(
    id integer NOT NULL,
    provider_name character varying(25) COLLATE pg_catalog."default",
    CONSTRAINT auth_providers_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.auth_providers
    OWNER to postgres;


-- -----------------------------------------------------
-- Table: coffeecompass.contact_me_message
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.contact_me_message;

CREATE TABLE coffeecompass.contact_me_message
(
    id serial NOT NULL,
	created_at time with time zone NOT NULL,
    author_name character varying(50) COLLATE pg_catalog."default",
    author_email character varying(60) COLLATE pg_catalog."default",
    message character varying(512) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT contact_me_message_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

	
-- -----------------------------------------------------	
-- Table: coffeecompass.images
-- -----------------------------------------------------
-- DROP TABLE coffeecompass.images;

CREATE TABLE coffeecompass.images
(
    id serial NOT NULL,
    coffeesite_id bigint NOT NULL,
    saved_on timestamp(6) with time zone NOT NULL,
    file_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    data bytea,
    CONSTRAINT images_pkey PRIMARY KEY (id),
    CONSTRAINT images_id_coffeesite_id_key UNIQUE (id, coffeesite_id)
,
    CONSTRAINT images_coffeesite_id_fkey FOREIGN KEY (coffeesite_id)
        REFERENCES coffeecompass.coffee_site (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.images
    OWNER to postgres;
COMMENT ON TABLE coffeecompass.images
    IS 'Saves images/photos of the CoffeeSites. Only one image can be assigned to one CoffeeSite.';

-- Index: fki_f

-- DROP INDEX coffeecompass.fki_f;

CREATE INDEX fki_f
    ON coffeecompass.images USING btree
    (coffeesite_id);
--    TABLESPACE pg_default;

	
	
-- -----------------------------------------------------	
-- Table: coffeecompass.password_reset_token
-- -----------------------------------------------------	
-- DROP TABLE coffeecompass.password_reset_token;

CREATE TABLE coffeecompass.password_reset_token
(
    id serial NOT NULL,
    user_id bigint,
    token character varying(512) COLLATE pg_catalog."default" NOT NULL,
    expiry_date timestamp without time zone,
    CONSTRAINT password_reset_token_pkey PRIMARY KEY (id),
    CONSTRAINT fk_user FOREIGN KEY (user_id)
        REFERENCES coffeecompass."user" (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
        NOT VALID
)
WITH (
    OIDS = FALSE
);
-- TABLESPACE pg_default;

ALTER TABLE coffeecompass.password_reset_token
    OWNER to postgres;
COMMENT ON TABLE coffeecompass.password_reset_token
    IS 'Unikatni "token", retezec pro reset hesla pomoci odkazu poslaneho do e-mailu uzivatele.';

    
-- -----------------------------------------------------	
-- Table: FIREBASE Topics and Tokens
-- -----------------------------------------------------	
-- Table: coffeecompass.firebase_topics

-- DROP TABLE coffeecompass.firebase_topics;

CREATE TABLE coffeecompass.firebase_topics
(
    id serial NOT NULL,
    main_topic character varying(64) COLLATE pg_catalog."default" NOT NULL,
    sub_topic character varying(64) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT firebase_topics_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE coffeecompass.firebase_topics
    OWNER to postgres;

GRANT INSERT, DELETE, UPDATE, SELECT ON TABLE coffeecompass.firebase_topics TO coffeecompass_dev_user;

GRANT DELETE, INSERT, SELECT, UPDATE ON TABLE coffeecompass.firebase_topics TO coffeecompass_prod_user;

GRANT ALL ON TABLE coffeecompass.firebase_topics TO postgres;

COMMENT ON TABLE coffeecompass.firebase_topics
    IS 'Topics which can be ordered to follow by user and will by updated by Firebase requests from coffeecompass.cz server.';


-- Table: coffeecompass.user_firebase_token

-- DROP TABLE coffeecompass.user_firebase_token;

CREATE TABLE coffeecompass.user_firebase_token
(
    id serial NOT NULL,
    user_id integer,
    firebase_token character varying(256) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT user_firebase_token_pkey PRIMARY KEY (id),
    CONSTRAINT user_firebase_token_firebase_token_key UNIQUE (firebase_token),
    CONSTRAINT user_id_fk FOREIGN KEY (user_id)
        REFERENCES coffeecompass."user" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE coffeecompass.user_firebase_token
    OWNER to postgres;

GRANT INSERT, DELETE, UPDATE, SELECT ON TABLE coffeecompass.user_firebase_token TO coffeecompass_dev_user;

GRANT DELETE, INSERT, SELECT, UPDATE ON TABLE coffeecompass.user_firebase_token TO coffeecompass_prod_user;

GRANT ALL ON TABLE coffeecompass.user_firebase_token TO postgres;

COMMENT ON TABLE coffeecompass.user_firebase_token
    IS 'Firebase token assigned to user''s application ''Kava s sebou'' when the app. is started. User id can be null!!! Every application/device can have Firebase token and order topic.';


-- Table: coffeecompass.subscription_token_to_topic

-- DROP TABLE coffeecompass.subscription_token_to_topic;

CREATE TABLE coffeecompass.subscription_token_to_topic
(
    firebase_token_id integer NOT NULL,
    firebase_topic_id integer NOT NULL,
    CONSTRAINT odbery_firebase_token_to_topic_pkey PRIMARY KEY (firebase_token_id, firebase_topic_id),
    CONSTRAINT fk_firebase_token FOREIGN KEY (firebase_token_id)
        REFERENCES coffeecompass.user_firebase_token (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID,
    CONSTRAINT fk_firebase_topic FOREIGN KEY (firebase_topic_id)
        REFERENCES coffeecompass.firebase_topics (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID
)

TABLESPACE pg_default;

ALTER TABLE coffeecompass.subscription_token_to_topic
    OWNER to postgres;

GRANT INSERT, DELETE, UPDATE, SELECT ON TABLE coffeecompass.subscription_token_to_topic TO coffeecompass_dev_user;

GRANT DELETE, INSERT, SELECT, UPDATE ON TABLE coffeecompass.subscription_token_to_topic TO coffeecompass_prod_user;

GRANT ALL ON TABLE coffeecompass.subscription_token_to_topic TO postgres;

COMMENT ON TABLE coffeecompass.subscription_token_to_topic
    IS 'Connection table to pair user''s firebase tokens with firebase topics. This ensures to send messages from Firebase to only user''s Kava s sebou applications which ordered the specific topic.';




-- -----------------------------------------------------
-- FUNCTIONS
-- -----------------------------------------------------

-- -----------------------------------------------------
-- function DISTANCE  -- vytvoří se v public schema
-- -----------------------------------------------------

--USE coffeecompass$$
CREATE OR REPLACE FUNCTION public.distance(lat1 double precision, lon1 double precision, lat2 double precision, lon2 double precision) RETURNS double precision AS '

  DECLARE 
  	dist    DOUBLE PRECISION;
  	latDist DOUBLE PRECISION;
  	lonDist DOUBLE PRECISION;
  	a DOUBLE PRECISION;
	c DOUBLE PRECISION;
	r DOUBLE PRECISION := 6372000; 

-- earths radius in meters
  --r := 6372000;

  BEGIN
  -- Haversine formula <http://en.wikipedia.org/wiki/Haversine_formula>
  latDist := RADIANS( lat2 - lat1 );
  lonDist := RADIANS( lon2 - lon1 );
  a := POW( SIN( latDist/2 ), 2 ) + COS( RADIANS( lat1 ) ) * COS( RADIANS( lat2 ) ) * POW( SIN( lonDist / 2 ), 2 );
  c  := 2 * ATAN2( SQRT( a ), SQRT( 1 - a ) );
  dist := r * c;  
  
  RETURN dist;
END;
' LANGUAGE 'plpgsql';

	
-- -----------------------------------------------------
-- function sitesWithinRange -- vytvoří se v public schema
-- -----------------------------------------------------		
-- FUNCTION: sitesWithinRange(double precision, double precision, bigint)

-- DROP FUNCTION public."sitesWithinRange"(double precision, double precision, bigint);

CREATE OR REPLACE FUNCTION sitesWithinRange(lat1 double precision,	lon1 double precision,	meters bigint) RETURNS refcursor AS '

-- Procedure that returns a single result set (cursor)

    DECLARE
      sites refcursor;                                                     -- Declare a cursor variable
    BEGIN
      OPEN sites FOR SELECT *, cs.poloha_gps_sirka, cs.poloha_gps_delka FROM coffeecompass.coffee_site AS cs WHERE distance(lat1, lon1, cs.poloha_gps_sirka, cs.poloha_gps_delka) < meters;   -- Open a cursor
      RETURN sites;                                                       -- Return the cursor to the caller
    END;

' LANGUAGE 'plpgsql';
