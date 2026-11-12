INSERT INTO public.roles (id, name)
SELECT gen_random_uuid(), v.name
FROM (VALUES ('CATALOG_ADMIN'), ('CATALOG_EDITOR'), ('CATALOG_READER'), ('TEACHER')) AS v(name)
WHERE NOT EXISTS (SELECT 1 FROM public.roles r WHERE r.name = v.name);