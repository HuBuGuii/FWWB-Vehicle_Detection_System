-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

CREATE TABLE public.camera (
                               camera_id bigint NOT NULL,
                               camera_name character varying(50) NOT NULL,
                               location character varying(100),
                               status character varying(10) NOT NULL,
                               ip_address character varying(255),
                               port integer,
                               protocol character varying(50),
                               source_type character varying(255),
                               device_id integer
);

CREATE SEQUENCE public.camera_cameraid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.camera_cameraid_seq OWNED BY public.camera.camera_id;

CREATE TABLE public."nonRealTimeDetectionRecord" (
                                                     nrd_id integer NOT NULL,
                                                     user_id integer NOT NULL,
                                                     "time" timestamp without time zone NOT NULL,
                                                     confidence numeric(5,2) NOT NULL,
                                                     vehicle_id integer NOT NULL,
                                                     vehicle_status character varying(10) NOT NULL,
                                                     max_age integer,
                                                     exp character varying(255)
);

CREATE SEQUENCE public.non_real_time_detection_record_nrdid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.non_real_time_detection_record_nrdid_seq OWNED BY public."nonRealTimeDetectionRecord".nrd_id;

CREATE TABLE public."realTimeDetectionRecord" (
                                                  rd_id integer NOT NULL,
                                                  camera_id integer NOT NULL,
                                                  confidence numeric(5,2) NOT NULL,
                                                  temperature numeric(5,2),
                                                  "time" timestamp without time zone NOT NULL,
                                                  vehicle_id integer NOT NULL,
                                                  vehicle_status character varying(10),
                                                  max_age integer,
                                                  weather character varying(10),
                                                  exp character varying(255)
);

CREATE SEQUENCE public.real_time_detection_record_rdid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.real_time_detection_record_rdid_seq OWNED BY public."realTimeDetectionRecord".rd_id;

CREATE TABLE public.role (
                             role_id bigint NOT NULL,
                             role_name character varying(255) NOT NULL
);

CREATE TABLE public.users (
                              user_id bigint NOT NULL,
                              account character varying(50) NOT NULL,
                              password character varying(255) NOT NULL,
                              contact character varying(20),
                              "position" character varying(50),
                              department character varying(50),
                              authorization_status character varying(20) NOT NULL,
                              real_name character varying(20) NOT NULL,
                              role_id bigint
);

CREATE SEQUENCE public.user_userid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.user_userid_seq OWNED BY public.users.user_id;

CREATE TABLE public.vehicle (
                                vehicle_id integer NOT NULL,
                                licence character varying(20),
                                type character varying
);

CREATE SEQUENCE public.vehicle_vehicleid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.vehicle_vehicleid_seq OWNED BY public.vehicle.vehicle_id;

ALTER TABLE ONLY public.camera ALTER COLUMN camera_id SET DEFAULT nextval('public.camera_cameraid_seq'::regclass);
ALTER TABLE ONLY public."nonRealTimeDetectionRecord" ALTER COLUMN nrd_id SET DEFAULT nextval('public.non_real_time_detection_record_nrdid_seq'::regclass);
ALTER TABLE ONLY public."realTimeDetectionRecord" ALTER COLUMN rd_id SET DEFAULT nextval('public.real_time_detection_record_rdid_seq'::regclass);
ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.user_userid_seq'::regclass);

-- Insert data for camera (excluding Network type cameras)
INSERT INTO public.camera (camera_id, camera_name, location, status, ip_address, port, protocol, source_type, device_id) VALUES
                                                                                                                             (5, 'Camera 4', 'Location 3', '正常', NULL, NULL, NULL, 'USB', 1),
                                                                                                                             (4, 'HP Wide Vision HD Camera', 'Location 2', '正常', NULL, NULL, NULL, 'USB', 0);

-- Insert data for role
INSERT INTO public.role (role_id, role_name) VALUES
                                                 (1, 'USER'),
                                                 (2, 'ADMIN');

-- Insert data for users (only admin and test2)
INSERT INTO public.users (user_id, account, password, contact, "position", department, authorization_status, real_name, role_id) VALUES
                                                                                                                                     (5, 'admin', '$2a$10$PzIDOmicRysZ2bXMELH4Ke9eY1Hfg/e1BHAjR357rev9kxsFz8/XS', 'admin@example.com', 'admin', 'admin', 'pass', 'TestadminUser_0', 2),
                                                                                                                                     (7, 'test2', '$2a$10$TTLpJIgdn1iShYwhipKIpewPtN6kUXxn1W3rzxLq0e6bx8UmLOXWy', 'test2@example.com', 'Developer', 'IT', 'pass', 'Test_2', 1);

-- Insert data for vehicle
INSERT INTO public.vehicle (vehicle_id, licence, type) VALUES
                                                           (1, 'Nah', 'person'),
                                                           (3, '', 'car'),
                                                           (8, NULL, 'truck'),
                                                           (10, NULL, 'traffic light'),
                                                           (5, NULL, 'airplane'),
                                                           (26, NULL, 'umbrella'),
                                                           (6, NULL, 'bus'),
                                                           (4, NULL, 'motorcycle'),
                                                           (991716523, NULL, 'person'),
                                                           (98260, NULL, 'car'),
                                                           (110640223, NULL, 'truck'),
                                                           (116515, NULL, 'van'),
                                                           (97920, NULL, 'bus');

SELECT pg_catalog.setval('public.camera_cameraid_seq', 8, true);
SELECT pg_catalog.setval('public.non_real_time_detection_record_nrdid_seq', 25026, true);
SELECT pg_catalog.setval('public.real_time_detection_record_rdid_seq', 764, true);
SELECT pg_catalog.setval('public.user_userid_seq', 10, true);
SELECT pg_catalog.setval('public.vehicle_vehicleid_seq', 1, true);

ALTER TABLE ONLY public.camera
    ADD CONSTRAINT camera_pkey PRIMARY KEY (camera_id);

ALTER TABLE ONLY public."nonRealTimeDetectionRecord"
    ADD CONSTRAINT non_real_time_detection_record_pkey PRIMARY KEY (nrd_id);

ALTER TABLE ONLY public."realTimeDetectionRecord"
    ADD CONSTRAINT real_time_detection_record_pkey PRIMARY KEY (rd_id);

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (role_id);

ALTER TABLE ONLY public.role
    ADD CONSTRAINT uk_iubw515ff0ugtm28p8g3myt0h UNIQUE (role_name);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT user_account_key UNIQUE (account);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT user_pkey PRIMARY KEY (user_id);

ALTER TABLE ONLY public.vehicle
    ADD CONSTRAINT vehicle_licence_key UNIQUE (licence);

ALTER TABLE ONLY public.vehicle
    ADD CONSTRAINT vehicle_pkey PRIMARY KEY (vehicle_id);

ALTER TABLE ONLY public."nonRealTimeDetectionRecord"
    ADD CONSTRAINT non_real_time_detection_record_userid_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);

ALTER TABLE ONLY public."nonRealTimeDetectionRecord"
    ADD CONSTRAINT non_real_time_detection_record_vehicleid_fkey FOREIGN KEY (vehicle_id) REFERENCES public.vehicle(vehicle_id);

ALTER TABLE ONLY public."realTimeDetectionRecord"
    ADD CONSTRAINT real_time_detection_record_cameraid_fkey FOREIGN KEY (camera_id) REFERENCES public.camera(camera_id);

ALTER TABLE ONLY public."realTimeDetectionRecord"
    ADD CONSTRAINT real_time_detection_record_vehicleid_fkey FOREIGN KEY (vehicle_id) REFERENCES public.vehicle(vehicle_id);