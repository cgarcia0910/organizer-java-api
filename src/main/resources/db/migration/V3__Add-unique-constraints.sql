-- Unicidad por tenant. Si ya existen datos duplicados, esta migración fallará:
-- límpialos antes de aplicarla.

-- Código de curso único por tenant
CREATE UNIQUE INDEX IF NOT EXISTS uq_courses_tenant_code
    ON public.courses (tenant_id, code);

-- Código de asignatura único dentro de un curso
CREATE UNIQUE INDEX IF NOT EXISTS uq_subjects_tenant_course_code
    ON public.subjects (tenant_id, course_id, code);

-- Email de profesor único por tenant, sin distinguir mayúsculas
CREATE UNIQUE INDEX IF NOT EXISTS uq_teachers_tenant_email
    ON public.teachers (tenant_id, lower(email));

-- Un profesor no puede tener dos habilitaciones ACTIVAS para el mismo curso y asignatura
CREATE UNIQUE INDEX IF NOT EXISTS uq_habilitations_active
    ON public.habilitations (tenant_id, teacher_id, course_id, subject_id)
    WHERE status = 'ACTIVE';
