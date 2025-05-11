ALTER TABLE public.reservation
    ADD COLUMN memo character varying(1000) COLLATE pg_catalog."default";

ALTER TABLE public.reservation
    ADD COLUMN member_count integer;

ALTER TABLE public.reservation
    ADD COLUMN remind_date date;

ALTER TABLE public.reservation
    ADD COLUMN remind_time time without time zone;
