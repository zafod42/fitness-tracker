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

-- Обычные упражнения
CREATE TABLE IF NOT EXISTS public.ordinaryExercise (
    id numeric(12,0) PRIMARY KEY,
    name text[],
    description text[],
    sets integer,
    repetitions integer,
    type text[]
);

-- Упражнения на время
CREATE TABLE IF NOT EXISTS public.timeExercise (
    id numeric(12,0) PRIMARY KEY,
    name text[],
    description text[],
    sets integer,
    timeInSeconds real,
    type text[]
);

-- Упражнения c весом
CREATE TABLE IF NOT EXISTS public.weightExercise (
    id numeric(12,0) PRIMARY KEY,
    name text[],
    description text[],
    sets integer,
    repetitions integer,
    weightPerRep real,
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

-- Обычные упражнения
INSERT INTO public.ordinaryExercise (id, name, description, sets, repetitions, type)
VALUES (1001, '{"Приседания"}', '{"упражнение для ног и ягодиц, выполняется, опускаясь в положение, как будто вы садитесь на стул, а затем поднимаетесь."}', 3, 20, '{Ordinary}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.ordinaryExercise (id, name, description, sets, repetitions, type)
VALUES (1002, '{"Подтягивания"}', '{"упражнение для верхней части тела, выполняется подтягиванием тела вверх, держась за перекладину."}', 2, 5, '{Ordinary}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.ordinaryExercise (id, name, description, sets, repetitions, type)
VALUES (1003, '{"Прыжки на скакалке"}', '{"кардиоупражнение, которое также тренирует координацию, выполняется с прыжками через скакалку."}', 3, 90, '{Ordinary}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.ordinaryExercise (id, name, description, sets, repetitions, type)
VALUES (1004, '{"Махи ногами"}', '{"упражнение для ног и ягодиц, выполняется махая ногой вперед и назад."}', 3, 20, '{Ordinary}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.ordinaryExercise (id, name, description, sets, repetitions, type)
VALUES (1005, '{"Лодка"}', '{"упражнение для спины и пресса, выполняется, лежа на животе и поднимая туловище и ноги от пола, формируя форму лодки."}', 3, 30, '{Ordinary}')
ON CONFLICT (id) DO NOTHING;


-- Упражнения на время
INSERT INTO public.timeExercise (id, name, description, sets, timeInSeconds, type)
VALUES (2001, '{"Планка на прямых руках"}', '{"статическая нагрузка мышц груди"}', 1, 10.0, '{Time}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.timeExercise (id, name, description, sets, timeInSeconds, type)
VALUES (2002, '{"Прыжки на скакалке"}', '{"кардиоупражнение, которое также тренирует координацию, выполняется с прыжками через скакалку."}', 3, 10.0, '{Time}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.timeExercise (id, name, description, sets, timeInSeconds, type)
VALUES (2003, '{"Бег на месте"}', '{"кардиоупражнение, которое можно выполнять дома, бегая на месте"}', 2, 30.0, '{Time}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.timeExercise (id, name, description, sets, timeInSeconds, type)
VALUES (2004, '{"Планка"}', '{"упражнение для укрепления кора"}', 2, 90.0, '{Time}')
ON CONFLICT (id) DO NOTHING;


-- Упражнения c весом
INSERT INTO public.weightExercise (id, name, description, sets, repetitions, weightPerRep, type)
VALUES (3001, '{"Жим лежа"}', '{"упражнение для развития грудных мышц"}', 2, 10, 25.0, '{Weight}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.weightExercise (id, name, description, sets, repetitions, weightPerRep, type)
VALUES (3002, '{"Подъём гантели"}', '{"чтоб бицуху качать"}', 6, 10, 10.0, '{Weight}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.users (exercises, info, chat_id) VALUES (ARRAY[1001, 1002], 'some info', 123453);
