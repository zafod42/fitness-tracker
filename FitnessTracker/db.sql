--
-- PostgreSQL database cluster dump
--

SET default_transaction_read_only = off;

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

--
-- Roles
--

CREATE ROLE postgres;
ALTER ROLE postgres WITH SUPERUSER INHERIT CREATEROLE CREATEDB LOGIN REPLICATION BYPASSRLS;

--
-- Databases
--

--
-- Database "template1" dump
--

\connect template1

--
-- PostgreSQL database dump
--

-- Dumped from database version 14.5
-- Dumped by pg_dump version 14.5

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

--
-- PostgreSQL database dump complete
--

--
-- Database "postgres" dump
--

\connect postgres

--
-- PostgreSQL database dump
--

-- Dumped from database version 14.5
-- Dumped by pg_dump version 14.5

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

--
-- Name: exercises; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exercises (
    id numeric(12,0),
    name text[],
    description text[],
    type text[]
);


ALTER TABLE public.exercises OWNER TO postgres;

--
-- Name: COLUMN exercises.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.exercises.id IS 'Идентификационный номер';


--
-- Name: COLUMN exercises.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.exercises.name IS 'Название упражнения';


--
-- Name: COLUMN exercises.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.exercises.description IS 'Описание упражнения';


--
-- Name: COLUMN exercises.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.exercises.type IS 'Тип упражнения';


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id integer,
    exercises integer[],
    info text,
    chat_id integer
);

ALTER TABLE public.users OWNER TO postgres;

--
-- Name: TABLE users; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.users IS 'Пользователи';


--
-- Data for Name: exercises; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.exercises VALUES (3001, '{"Прыжки в длину"}', '{"очень полезное упражнение - мамой клянусь"}', '{Flexible}');
INSERT INTO public.exercises VALUES (1001, '{"Планка на прямых руках"}', '{"статическая нагрузка мышц груди"}', '{Strength}');
INSERT INTO public.exercises VALUES (1002, '{Отжимания}', '{"упражнение для верхней части тела. Выполняется","когда лицо опущено вниз","и руки отталкивают тело от земли."}', '{Strength}');
INSERT INTO public.exercises VALUES (1003, '{Приседания}', '{"упражнение для ног и ягодиц",выполняется,"опускаясь в положение","как будто вы садитесь на стул","а затем поднимаетесь."}', '{Strength}');
INSERT INTO public.exercises VALUES (2001, '{"Бег на месте"}', '{кардиоупражнение,"которое можно выполнять дома","бегая на месте."}', '{Cardio}');


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.users VALUES (0, '{}', 'admin');


--
-- Name: exercises exercises_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exercises
    ADD CONSTRAINT exercises_id_key UNIQUE (id);


--
-- Name: users users_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_id_key UNIQUE (id);

--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database cluster dump complete
--
