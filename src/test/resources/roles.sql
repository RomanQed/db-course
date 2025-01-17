DO
$do$
BEGIN
   IF EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = '_service') THEN
ELSE
      BEGIN
CREATE ROLE _service SUPERUSER;
EXCEPTION
         WHEN duplicate_object THEN
END;
END IF;
END
$do$;

DO
$do$
BEGIN
   IF EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = '_admin') THEN
ELSE
      BEGIN
CREATE ROLE _admin SUPERUSER;
EXCEPTION
         WHEN duplicate_object THEN
END;
END IF;
END
$do$;

DO
$do$
BEGIN
   IF EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = '_user') THEN
ELSE
      BEGIN   -- nested block
CREATE ROLE _user SUPERUSER;
EXCEPTION
         WHEN duplicate_object THEN
END;
END IF;
END
$do$;
