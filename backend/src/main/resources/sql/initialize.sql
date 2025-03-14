PGDMP      2    
            }            fwwb    16.7    16.7 1    L           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                      false            M           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                      false            N           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                      false            O           1262    16388    fwwb    DATABASE     j   CREATE DATABASE fwwb WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US';
    DROP DATABASE fwwb;
                13168    false            P           0    0    DATABASE fwwb    COMMENT     J   COMMENT ON DATABASE fwwb IS 'Database for FWWB Vehicle Detection System';
                   13168    false    4943            �            1259    16455    camera    TABLE     �   CREATE TABLE public.camera (
    "cameraId" integer NOT NULL,
    "cameraName" character varying(50) NOT NULL,
    location character varying(100),
    status character varying(10) NOT NULL
);
    DROP TABLE public.camera;
       public         heap    13168    false            �            1259    16454    camera_cameraid_seq    SEQUENCE     �   CREATE SEQUENCE public.camera_cameraid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 *   DROP SEQUENCE public.camera_cameraid_seq;
       public          13168    false    220            Q           0    0    camera_cameraid_seq    SEQUENCE OWNED BY     M   ALTER SEQUENCE public.camera_cameraid_seq OWNED BY public.camera."cameraId";
          public          13168    false    219            �            1259    16479    nonRealTimeDetectionRecord    TABLE     5  CREATE TABLE public."nonRealTimeDetectionRecord" (
    "nrdId" integer NOT NULL,
    "userId" integer NOT NULL,
    "time" timestamp without time zone NOT NULL,
    confidence numeric(5,2) NOT NULL,
    "vehicleId" integer NOT NULL,
    "vehicleStatus" character varying(10) NOT NULL,
    "maxAge" integer
);
 0   DROP TABLE public."nonRealTimeDetectionRecord";
       public         heap    13168    false            �            1259    16478 (   non_real_time_detection_record_nrdid_seq    SEQUENCE     �   CREATE SEQUENCE public.non_real_time_detection_record_nrdid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 ?   DROP SEQUENCE public.non_real_time_detection_record_nrdid_seq;
       public          13168    false    224            R           0    0 (   non_real_time_detection_record_nrdid_seq    SEQUENCE OWNED BY     u   ALTER SEQUENCE public.non_real_time_detection_record_nrdid_seq OWNED BY public."nonRealTimeDetectionRecord"."nrdId";
          public          13168    false    223            �            1259    16462    realTimeDetectionRecord    TABLE     t  CREATE TABLE public."realTimeDetectionRecord" (
    "rdId" integer NOT NULL,
    "cameraId" integer NOT NULL,
    confidence numeric(5,2) NOT NULL,
    temperature numeric(5,2),
    "time" timestamp without time zone NOT NULL,
    "vehicleId" integer NOT NULL,
    "vehicleStatus" character varying(10) NOT NULL,
    "maxAge" integer,
    weather character varying(10)
);
 -   DROP TABLE public."realTimeDetectionRecord";
       public         heap    13168    false            �            1259    16461 #   real_time_detection_record_rdid_seq    SEQUENCE     �   CREATE SEQUENCE public.real_time_detection_record_rdid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 :   DROP SEQUENCE public.real_time_detection_record_rdid_seq;
       public          13168    false    222            S           0    0 #   real_time_detection_record_rdid_seq    SEQUENCE OWNED BY     l   ALTER SEQUENCE public.real_time_detection_record_rdid_seq OWNED BY public."realTimeDetectionRecord"."rdId";
          public          13168    false    221            �            1259    16502    role    TABLE     k   CREATE TABLE public.role (
    "roleId" integer NOT NULL,
    "roleName" character varying(20) NOT NULL
);
    DROP TABLE public.role;
       public         heap    13168    false            �            1259    16437    user    TABLE     �  CREATE TABLE public."user" (
    "userId" integer NOT NULL,
    account character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    contact character varying(20),
    "position" character varying(50),
    department character varying(50),
    "authorizationStatus" character varying(20) NOT NULL,
    "realName" character varying(20) NOT NULL,
    "roleId" integer
);
    DROP TABLE public."user";
       public         heap    13168    false            �            1259    16436    user_userid_seq    SEQUENCE     �   CREATE SEQUENCE public.user_userid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 &   DROP SEQUENCE public.user_userid_seq;
       public          13168    false    216            T           0    0    user_userid_seq    SEQUENCE OWNED BY     G   ALTER SEQUENCE public.user_userid_seq OWNED BY public."user"."userId";
          public          13168    false    215            �            1259    16446    vehicle    TABLE     �   CREATE TABLE public.vehicle (
    "vehicleId" integer NOT NULL,
    licence character varying(20) NOT NULL,
    type character varying
);
    DROP TABLE public.vehicle;
       public         heap    13168    false            �            1259    16445    vehicle_vehicleid_seq    SEQUENCE     �   CREATE SEQUENCE public.vehicle_vehicleid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 ,   DROP SEQUENCE public.vehicle_vehicleid_seq;
       public          13168    false    218            U           0    0    vehicle_vehicleid_seq    SEQUENCE OWNED BY     Q   ALTER SEQUENCE public.vehicle_vehicleid_seq OWNED BY public.vehicle."vehicleId";
          public          13168    false    217            �           2604    16458    camera cameraId    DEFAULT     t   ALTER TABLE ONLY public.camera ALTER COLUMN "cameraId" SET DEFAULT nextval('public.camera_cameraid_seq'::regclass);
 @   ALTER TABLE public.camera ALTER COLUMN "cameraId" DROP DEFAULT;
       public          13168    false    219    220    220            �           2604    16482     nonRealTimeDetectionRecord nrdId    DEFAULT     �   ALTER TABLE ONLY public."nonRealTimeDetectionRecord" ALTER COLUMN "nrdId" SET DEFAULT nextval('public.non_real_time_detection_record_nrdid_seq'::regclass);
 S   ALTER TABLE public."nonRealTimeDetectionRecord" ALTER COLUMN "nrdId" DROP DEFAULT;
       public          13168    false    224    223    224            �           2604    16465    realTimeDetectionRecord rdId    DEFAULT     �   ALTER TABLE ONLY public."realTimeDetectionRecord" ALTER COLUMN "rdId" SET DEFAULT nextval('public.real_time_detection_record_rdid_seq'::regclass);
 O   ALTER TABLE public."realTimeDetectionRecord" ALTER COLUMN "rdId" DROP DEFAULT;
       public          13168    false    221    222    222            �           2604    16440    user userId    DEFAULT     n   ALTER TABLE ONLY public."user" ALTER COLUMN "userId" SET DEFAULT nextval('public.user_userid_seq'::regclass);
 >   ALTER TABLE public."user" ALTER COLUMN "userId" DROP DEFAULT;
       public          13168    false    215    216    216            �           2604    16449    vehicle vehicleId    DEFAULT     x   ALTER TABLE ONLY public.vehicle ALTER COLUMN "vehicleId" SET DEFAULT nextval('public.vehicle_vehicleid_seq'::regclass);
 B   ALTER TABLE public.vehicle ALTER COLUMN "vehicleId" DROP DEFAULT;
       public          13168    false    217    218    218            D          0    16455    camera 
   TABLE DATA           L   COPY public.camera ("cameraId", "cameraName", location, status) FROM stdin;
    public          13168    false    220   �<       H          0    16479    nonRealTimeDetectionRecord 
   TABLE DATA           �   COPY public."nonRealTimeDetectionRecord" ("nrdId", "userId", "time", confidence, "vehicleId", "vehicleStatus", "maxAge") FROM stdin;
    public          13168    false    224   
=       F          0    16462    realTimeDetectionRecord 
   TABLE DATA           �   COPY public."realTimeDetectionRecord" ("rdId", "cameraId", confidence, temperature, "time", "vehicleId", "vehicleStatus", "maxAge", weather) FROM stdin;
    public          13168    false    222   '=       I          0    16502    role 
   TABLE DATA           4   COPY public.role ("roleId", "roleName") FROM stdin;
    public          13168    false    225   D=       @          0    16437    user 
   TABLE DATA           �   COPY public."user" ("userId", account, password, contact, "position", department, "authorizationStatus", "realName", "roleId") FROM stdin;
    public          13168    false    216   a=       B          0    16446    vehicle 
   TABLE DATA           =   COPY public.vehicle ("vehicleId", licence, type) FROM stdin;
    public          13168    false    218   ~=       V           0    0    camera_cameraid_seq    SEQUENCE SET     B   SELECT pg_catalog.setval('public.camera_cameraid_seq', 1, false);
          public          13168    false    219            W           0    0 (   non_real_time_detection_record_nrdid_seq    SEQUENCE SET     W   SELECT pg_catalog.setval('public.non_real_time_detection_record_nrdid_seq', 1, false);
          public          13168    false    223            X           0    0 #   real_time_detection_record_rdid_seq    SEQUENCE SET     R   SELECT pg_catalog.setval('public.real_time_detection_record_rdid_seq', 1, false);
          public          13168    false    221            Y           0    0    user_userid_seq    SEQUENCE SET     >   SELECT pg_catalog.setval('public.user_userid_seq', 1, false);
          public          13168    false    215            Z           0    0    vehicle_vehicleid_seq    SEQUENCE SET     D   SELECT pg_catalog.setval('public.vehicle_vehicleid_seq', 1, false);
          public          13168    false    217            �           2606    16460    camera camera_pkey 
   CONSTRAINT     X   ALTER TABLE ONLY public.camera
    ADD CONSTRAINT camera_pkey PRIMARY KEY ("cameraId");
 <   ALTER TABLE ONLY public.camera DROP CONSTRAINT camera_pkey;
       public            13168    false    220            �           2606    16484 >   nonRealTimeDetectionRecord non_real_time_detection_record_pkey 
   CONSTRAINT     �   ALTER TABLE ONLY public."nonRealTimeDetectionRecord"
    ADD CONSTRAINT non_real_time_detection_record_pkey PRIMARY KEY ("nrdId");
 j   ALTER TABLE ONLY public."nonRealTimeDetectionRecord" DROP CONSTRAINT non_real_time_detection_record_pkey;
       public            13168    false    224            �           2606    16467 7   realTimeDetectionRecord real_time_detection_record_pkey 
   CONSTRAINT     {   ALTER TABLE ONLY public."realTimeDetectionRecord"
    ADD CONSTRAINT real_time_detection_record_pkey PRIMARY KEY ("rdId");
 c   ALTER TABLE ONLY public."realTimeDetectionRecord" DROP CONSTRAINT real_time_detection_record_pkey;
       public            13168    false    222            �           2606    16506    role role_pkey 
   CONSTRAINT     R   ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY ("roleId");
 8   ALTER TABLE ONLY public.role DROP CONSTRAINT role_pkey;
       public            13168    false    225            �           2606    16444    user user_account_key 
   CONSTRAINT     U   ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_account_key UNIQUE (account);
 A   ALTER TABLE ONLY public."user" DROP CONSTRAINT user_account_key;
       public            13168    false    216            �           2606    16442    user user_pkey 
   CONSTRAINT     T   ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_pkey PRIMARY KEY ("userId");
 :   ALTER TABLE ONLY public."user" DROP CONSTRAINT user_pkey;
       public            13168    false    216            �           2606    16453    vehicle vehicle_licence_key 
   CONSTRAINT     Y   ALTER TABLE ONLY public.vehicle
    ADD CONSTRAINT vehicle_licence_key UNIQUE (licence);
 E   ALTER TABLE ONLY public.vehicle DROP CONSTRAINT vehicle_licence_key;
       public            13168    false    218            �           2606    16451    vehicle vehicle_pkey 
   CONSTRAINT     [   ALTER TABLE ONLY public.vehicle
    ADD CONSTRAINT vehicle_pkey PRIMARY KEY ("vehicleId");
 >   ALTER TABLE ONLY public.vehicle DROP CONSTRAINT vehicle_pkey;
       public            13168    false    218            �           2606    16485 E   nonRealTimeDetectionRecord non_real_time_detection_record_userid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public."nonRealTimeDetectionRecord"
    ADD CONSTRAINT non_real_time_detection_record_userid_fkey FOREIGN KEY ("userId") REFERENCES public."user"("userId");
 q   ALTER TABLE ONLY public."nonRealTimeDetectionRecord" DROP CONSTRAINT non_real_time_detection_record_userid_fkey;
       public          13168    false    4767    224    216            �           2606    16490 H   nonRealTimeDetectionRecord non_real_time_detection_record_vehicleid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public."nonRealTimeDetectionRecord"
    ADD CONSTRAINT non_real_time_detection_record_vehicleid_fkey FOREIGN KEY ("vehicleId") REFERENCES public.vehicle("vehicleId");
 t   ALTER TABLE ONLY public."nonRealTimeDetectionRecord" DROP CONSTRAINT non_real_time_detection_record_vehicleid_fkey;
       public          13168    false    224    218    4771            �           2606    16468 @   realTimeDetectionRecord real_time_detection_record_cameraid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public."realTimeDetectionRecord"
    ADD CONSTRAINT real_time_detection_record_cameraid_fkey FOREIGN KEY ("cameraId") REFERENCES public.camera("cameraId");
 l   ALTER TABLE ONLY public."realTimeDetectionRecord" DROP CONSTRAINT real_time_detection_record_cameraid_fkey;
       public          13168    false    220    4773    222            �           2606    16473 A   realTimeDetectionRecord real_time_detection_record_vehicleid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public."realTimeDetectionRecord"
    ADD CONSTRAINT real_time_detection_record_vehicleid_fkey FOREIGN KEY ("vehicleId") REFERENCES public.vehicle("vehicleId");
 m   ALTER TABLE ONLY public."realTimeDetectionRecord" DROP CONSTRAINT real_time_detection_record_vehicleid_fkey;
       public          13168    false    4771    218    222            D      x������ � �      H      x������ � �      F      x������ � �      I      x������ � �      @      x������ � �      B      x������ � �     