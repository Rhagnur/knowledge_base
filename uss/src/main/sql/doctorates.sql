DROP TABLE IF EXISTS fo.doctorate CASCADE;
DROP TABLE IF EXISTS fo.doctorate_categories;
DROP TABLE IF EXISTS fo.doctorate_people CASCADE;
DROP TABLE IF EXISTS fo.doctorate_people_extern;
DROP TABLE IF EXISTS fo.doctorate_attachments;
DROP TABLE IF EXISTS fo.doctorate_graduations;
DROP TABLE IF EXISTS fo.doctorate_people_intern;
DROP TABLE IF EXISTS fo.doctorate_published;

DROP TYPE fo.supervision_types;
DROP TYPE fo.attach_types;
DROP TYPE fo.financing_types;
DROP TYPE fo.status_types;
DROP TYPE fo.graduation_types;
DROP TYPE fo.uni_categories;

CREATE TYPE fo.supervision_types AS ENUM ('first_supervisor', 'first_assessor', 'first_examiner', 'second_supervisor', 'second_assessor', 'second_examiner');
CREATE TYPE fo.financing_types AS ENUM ('stip', 'lehra', 'wissmit', 'drittmittel', 'forschförd_htw', 'beruf', 'priv', 'htw_booster', 'misc');
CREATE TYPE fo.attach_types AS ENUM ('expose', 'cooperation_contract', 'supervision_agreement', 'dissertation');
CREATE TYPE fo.graduation_types AS ENUM ('ma_fh', 'ma_uni', 'ba_fh', 'ba_uni', 'dipl_fh', 'dipl_uni', 'staatsex');
CREATE TYPE fo.status_types AS ENUM ('wip', 'fail' , 'done', 'abort');
CREATE TYPE fo.uni_categories AS ENUM ('berlin', 'ger', 'foreign');

ALTER TYPE fo.supervision_types OWNER TO fo;
ALTER TYPE fo.attach_types OWNER TO fo;
ALTER TYPE fo.financing_types OWNER TO fo;
ALTER TYPE fo.status_types OWNER TO fo;
ALTER TYPE fo.graduation_types OWNER TO fo;
ALTER TYPE fo.uni_categories OWNER TO fo;


CREATE TABLE fo.doctorate (
  id SERIAL NOT NULL PRIMARY KEY,
  created TIMESTAMP NOT NULL DEFAULT now(),
  lastmodified TIMESTAMP NOT NULL DEFAULT now(),
  accepted TIMESTAMP,
  finished TIMESTAMP,
  title text NOT NULL,
  status fo.status_types NOT NULL,
  doctorand_sn text NOT NULL,
  doctorand_givenname text NOT NULL,
  doctorand_university_name text NOT NULL,
  doctorand_graduation_date TIMESTAMP NOT NULL,
  university_name text NOT NULL,
  university_category fo.uni_categories NOT NULL,
  university_place text NOT NULL,
  main_financing fo.financing_types NOT NULL,
  second_financing fo.financing_types
);
ALTER TABLE fo.doctorate OWNER to fo;


CREATE TABLE fo.doctorate_categories (
  doct_id INTEGER NOT NULL REFERENCES  fo.doctorate(id) ON DELETE CASCADE,
  cat_id INTEGER NOT NULL REFERENCES public.categories(category_id) ON DELETE CASCADE,
  PRIMARY KEY (doct_id, cat_id)
);
ALTER TABLE fo.doctorate_categories OWNER to fo;


CREATE TABLE fo.doctorate_people (
  doct_id INTEGER NOT NULL REFERENCES fo.doctorate(id) ON DELETE CASCADE,
  supervision_category fo.supervision_types NOT NULL,
  PRIMARY KEY (doct_id, supervision_category)
);
ALTER TABLE fo.doctorate_people OWNER to fo;


CREATE TABLE fo.doctorate_people_intern (
  pvz_id TEXT NOT NULL REFERENCES pvz.persons(person_id) ON DELETE CASCADE
) INHERITS (fo.doctorate_people);
ALTER TABLE fo.doctorate_people_intern OWNER TO fo;


CREATE TABLE fo.doctorate_people_extern (
  sn TEXT NOT NULL,
  givenname TEXT NOT NULL,
  title text,
  faculty text,
  field text
) INHERITS (fo.doctorate_people);
ALTER TABLE fo.doctorate_people_extern OWNER to fo;


CREATE TABLE fo.doctorate_attachments (
  doct_id INTEGER REFERENCES fo.doctorate(id) ON DELETE CASCADE,
  attach_category fo.attach_types NOT NULL,
  date TIMESTAMP NOT NULL DEFAULT now(),
  rawdata BYTEA,
  size BIGINT,
  mime_type text,
  link text,
  filename text,
  PRIMARY KEY (doct_id, attach_category)
);
ALTER TABLE fo.doctorate_attachments OWNER to fo;


CREATE TABLE fo.doctorate_graduations(
  id SERIAL NOT NULL PRIMARY KEY,
  doct_id INTEGER NOT NULL REFERENCES fo.doctorate(id) ON DELETE CASCADE,
  type fo.graduation_types NOT NULL
);
ALTER TABLE fo.doctorate_graduations OWNER to fo;


CREATE TABLE fo.doctorate_published (
  doct_id INTEGER REFERENCES fo.doctorate(id) ON DELETE CASCADE PRIMARY KEY,
  author text NOT NULL,
  title text NOT NULL,
  place text NOT NULL,
  isbn text NOT NULL,
  published_date TIMESTAMP NOT NULL,
  link text
);
ALTER TABLE fo.doctorate_published OWNER to fo;