SET default_transaction_read_only = off;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'postgres') THEN
            CREATE ROLE postgres WITH SUPERUSER INHERIT CREATEROLE CREATEDB LOGIN REPLICATION BYPASSRLS;
        END IF;
    END
$$;

\connect template1

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

\connect postgres

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

CREATE TABLE IF NOT EXISTS public.exercises (
    id numeric(12,0) PRIMARY KEY,
    name text[],
    description text[],
    type text[]
);

CREATE TABLE IF NOT EXISTS public.users (
    id SERIAL PRIMARY KEY,
    exercises integer[],
    info text,
    chat_id integer UNIQUE
);

ALTER TABLE public.users
    ALTER COLUMN chat_id TYPE BIGINT USING (chat_id::BIGINT);

COMMENT ON TABLE public.users IS 'Пользователи';

INSERT INTO public.exercises (id, name, description, type)
VALUES (3001, '{"Прыжки в длину"}', '{"очень полезное упражнение - мамой клянусь"}', '{Flexible}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.exercises (id, name, description, type)
VALUES (1001, '{"Планка на прямых руках"}', '{"статическая нагрузка мышц груди"}', '{Strength}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.exercises (id, name, description, type)
VALUES (1002, '{Отжимания}', '{"упражнение для верхней части тела. Выполняется","когда лицо опущено вниз","и руки отталкивают тело от земли."}', '{Strength}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.exercises (id, name, description, type)
VALUES (1003, '{Приседания}', '{"упражнение для ног и ягодиц",выполняется,"опускаясь в положение","как будто вы садитесь на стул","а затем поднимаетесь."}', '{Strength}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.exercises (id, name, description, type)
VALUES (2001, '{"Бег на месте"}', '{кардиоупражнение,"которое можно выполнять дома","бегая на месте."}', '{Cardio}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.users (exercises, info, chat_id) VALUES (ARRAY[1001, 1002], 'some info', 123456);
