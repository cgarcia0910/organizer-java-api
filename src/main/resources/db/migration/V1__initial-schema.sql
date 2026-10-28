
-- Tenants
CREATE TABLE public.tenants (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                name VARCHAR NOT NULL,
                                CONSTRAINT uq_tenants_id_id UNIQUE (id)
);

-- Users
CREATE TABLE public.app_users (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  email VARCHAR NOT NULL UNIQUE,
                                  password VARCHAR NOT NULL,
                                  role VARCHAR NOT NULL,
                                  tenant_id UUID NOT NULL,
                                  CONSTRAINT fk_app_users_tenant
                                      FOREIGN KEY (tenant_id) REFERENCES public.tenants(id)
);

-- Courses
CREATE TABLE public.courses (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                tenant_id UUID NOT NULL,
                                code VARCHAR NOT NULL CHECK (length(btrim(code)) > 0),
                                name VARCHAR NOT NULL CHECK (length(btrim(name)) > 0),
                                description VARCHAR,
                                status VARCHAR NOT NULL DEFAULT 'ACTIVE'
                                    CHECK (status IN ('ACTIVE', 'INACTIVE')),
                                created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                CONSTRAINT uq_courses_tenant_id_id UNIQUE (tenant_id, id),
                                CONSTRAINT fk_courses_tenant
                                    FOREIGN KEY (tenant_id) REFERENCES public.tenants(id)
);

-- Subjects
CREATE TABLE public.subjects (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 tenant_id UUID NOT NULL,
                                 course_id UUID NOT NULL,
                                 code VARCHAR NOT NULL CHECK (length(btrim(code)) > 0),
                                 name VARCHAR NOT NULL CHECK (length(btrim(name)) > 0),
                                 description VARCHAR,
                                 status VARCHAR NOT NULL DEFAULT 'ACTIVE'
                                     CHECK (status IN ('ACTIVE', 'INACTIVE')),
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                 CONSTRAINT uq_subjects_tenant_id_id UNIQUE (tenant_id, id),
                                 CONSTRAINT uq_subjects_tenant_course_id UNIQUE (tenant_id, course_id, id),

                                 CONSTRAINT fk_subjects_course
                                     FOREIGN KEY (tenant_id, course_id)
                                         REFERENCES public.courses(tenant_id, id)
);

-- Teachers
CREATE TABLE public.teachers (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 tenant_id UUID NOT NULL,
                                 first_name VARCHAR NOT NULL CHECK (length(btrim(first_name)) > 0),
                                 last_name VARCHAR NOT NULL CHECK (length(btrim(last_name)) > 0),
                                 email VARCHAR NOT NULL CHECK (length(btrim(email)) > 0),
                                 status VARCHAR NOT NULL DEFAULT 'ACTIVE'
                                     CHECK (status IN ('ACTIVE', 'INACTIVE')),
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                 CONSTRAINT uq_teachers_tenant_id_id UNIQUE (tenant_id, id),
                                 CONSTRAINT fk_teachers_tenant
                                     FOREIGN KEY (tenant_id) REFERENCES public.tenants(id)
);

-- Teacher identities
CREATE TABLE public.teacher_identities (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           tenant_id UUID NOT NULL,
                                           teacher_id UUID NOT NULL,
                                           issuer VARCHAR NOT NULL CHECK (length(btrim(issuer)) > 0),
                                           identity_subject VARCHAR NOT NULL
                                               CHECK (length(btrim(identity_subject)) > 0),
                                           created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                           CONSTRAINT fk_teacher_identities_teacher
                                               FOREIGN KEY (tenant_id, teacher_id)
                                                   REFERENCES public.teachers(tenant_id, id),

                                           CONSTRAINT uq_teacher_identities_issuer_subject
                                               UNIQUE (tenant_id, issuer, identity_subject)
);

-- Habilitations
CREATE TABLE public.habilitations (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      tenant_id UUID NOT NULL,
                                      teacher_id UUID NOT NULL,
                                      course_id UUID NOT NULL,
                                      subject_id UUID NOT NULL,
                                      status VARCHAR NOT NULL DEFAULT 'ACTIVE'
                                          CHECK (status IN ('ACTIVE', 'REVOKED')),
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                      revoked_at TIMESTAMPTZ,

                                      CONSTRAINT fk_habilitations_teacher
                                          FOREIGN KEY (tenant_id, teacher_id)
                                              REFERENCES public.teachers(tenant_id, id),

                                      CONSTRAINT fk_habilitations_course
                                          FOREIGN KEY (tenant_id, course_id)
                                              REFERENCES public.courses(tenant_id, id),

                                      CONSTRAINT fk_habilitations_subject_course
                                          FOREIGN KEY (tenant_id, course_id, subject_id)
                                              REFERENCES public.subjects(tenant_id, course_id, id)
);

-- Roles
CREATE TABLE public.roles (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              name VARCHAR NOT NULL UNIQUE
);

-- User roles
CREATE TABLE public.app_user_roles (
                                       user_id UUID NOT NULL,
                                       role_id UUID NOT NULL,

                                       CONSTRAINT app_user_roles_pkey PRIMARY KEY (user_id, role_id),

                                       CONSTRAINT fk_app_user_roles_user
                                           FOREIGN KEY (user_id) REFERENCES public.app_users(id),

                                       CONSTRAINT fk_app_user_roles_role
                                           FOREIGN KEY (role_id) REFERENCES public.roles(id)
);
