CREATE TYPE "profile_type" AS ENUM (
  'user',
  'organization'
);

CREATE TABLE "follows" (
  "follower_profile_id" uuid NOT NULL,
  "following_profile_id" uuid NOT NULL,
  "created_at" timestamptz NOT NULL,
  PRIMARY KEY ("follower_profile_id", "following_profile_id")
);

CREATE TABLE "profiles" (
  "id" uuid PRIMARY KEY NOT NULL,
  "username" varchar(50) UNIQUE,
  "name" varchar(150),
  "email" varchar(255) UNIQUE NOT NULL,
  "password" varchar(255) NOT NULL,
  "public_email" varchar(255) DEFAULT null,
  "type" varchar(50) NOT NULL,
  "followers_count" integer DEFAULT 0,
  "following_count" integer DEFAULT 0,
  "created_at" timestamptz NOT NULL,
  "updated_at" timestamptz DEFAULT null,
  "deleted_at" timestamptz DEFAULT null
);

CREATE TABLE "posts" (
  "id" uuid PRIMARY KEY NOT NULL,
  "profile_id" uuid NOT NULL,
  "img_link" varchar(255) DEFAULT null,
  "description" text,
  "likes_count" integer,
  "created_at" timestamptz NOT NULL
);

CREATE TABLE "events" (
  "id" uuid PRIMARY KEY NOT NULL,
  "profile_id" uuid NOT NULL,
  "title" varchar(255) NOT NULL,
  "img_link" varchar(255),
  "event_time" timestamptz NOT NULL,
  "description" text,
  "street_address" varchar(150),
  "administrative_region" varchar(150),
  "city" varchar(150),
  "state" varchar(150),
  "reference_point" varchar(255),
  "latitude" decimal(10,6),
  "longitude" decimal(11,6),
  "created_at" timestamptz NOT NULL,
  "finished_at" timestamptz
);

CREATE TABLE "likes" (
  "user_id" uuid,
  "post_id" uuid,
  "created_at" timestamptz NOT NULL,
  PRIMARY KEY ("user_id", "post_id")
);

CREATE TABLE "shareds" (
  "id" uuid PRIMARY KEY NOT NULL,
  "profile_sharing_id" uuid NOT NULL,
  "profile_shared_id" uuid NOT NULL,
  "post_id" uuid,
  "created_at" timestamptz NOT NULL
);

CREATE TABLE "memberships" (
  "id" uuid PRIMARY KEY NOT NULL,
  "profile_id" uuid NOT NULL,
  "organization_id" uuid NOT NULL,
  "role" varchar DEFAULT 'member',
  "created_at" timestamptz NOT NULL
);

CREATE TABLE "event_participants" (
  "profile_id" uuid,
  "event_id" uuid,
  "created_at" timestamptz NOT NULL,
  PRIMARY KEY ("profile_id", "event_id")
);

CREATE TABLE "artist_products" (
  "id" uuid PRIMARY KEY NOT NULL,
  "profile_id" uuid NOT NULL,
  "title" varchar(255) NOT NULL,
  "img_link" varchar(255),
  "price" decimal(10,2) NOT NULL,
  "created_at" timestamptz NOT NULL,
  "deleted_at" timestamptz
);

COMMENT ON COLUMN "follows"."follower_profile_id" IS 'quem esta seguindo';

COMMENT ON COLUMN "follows"."following_profile_id" IS 'quem esta sendo seguido';

COMMENT ON COLUMN "profiles"."type" IS 'user | organization';

COMMENT ON COLUMN "posts"."description" IS 'descrição do post';

COMMENT ON COLUMN "likes"."user_id" IS 'quem curtiu';

COMMENT ON COLUMN "likes"."post_id" IS 'post curtido';

COMMENT ON COLUMN "shareds"."profile_sharing_id" IS 'id do usuario que esta compartilhando o post';

COMMENT ON COLUMN "shareds"."profile_shared_id" IS 'id do usuario que recebera o post';

COMMENT ON COLUMN "shareds"."post_id" IS 'id do post';

COMMENT ON COLUMN "memberships"."role" IS 'member | admin';

COMMENT ON COLUMN "event_participants"."profile_id" IS 'quem confirmou presença';

COMMENT ON COLUMN "event_participants"."event_id" IS 'evento em que o perfil vai participar';

COMMENT ON COLUMN "artist_products"."profile_id" IS 'artista que está vendendo';

ALTER TABLE "follows" ADD FOREIGN KEY ("follower_profile_id") REFERENCES "profiles" ("id");

ALTER TABLE "follows" ADD FOREIGN KEY ("following_profile_id") REFERENCES "profiles" ("id");

ALTER TABLE "posts" ADD FOREIGN KEY ("profile_id") REFERENCES "profiles" ("id");

ALTER TABLE "events" ADD FOREIGN KEY ("profile_id") REFERENCES "profiles" ("id");

ALTER TABLE "likes" ADD FOREIGN KEY ("user_id") REFERENCES "profiles" ("id");

ALTER TABLE "likes" ADD FOREIGN KEY ("post_id") REFERENCES "posts" ("id");

ALTER TABLE "shareds" ADD FOREIGN KEY ("profile_sharing_id") REFERENCES "profiles" ("id");

ALTER TABLE "shareds" ADD FOREIGN KEY ("profile_shared_id") REFERENCES "profiles" ("id");

ALTER TABLE "shareds" ADD FOREIGN KEY ("post_id") REFERENCES "posts" ("id");

ALTER TABLE "memberships" ADD FOREIGN KEY ("profile_id") REFERENCES "profiles" ("id");

ALTER TABLE "memberships" ADD FOREIGN KEY ("organization_id") REFERENCES "profiles" ("id");

ALTER TABLE "event_participants" ADD FOREIGN KEY ("profile_id") REFERENCES "profiles" ("id");

ALTER TABLE "event_participants" ADD FOREIGN KEY ("event_id") REFERENCES "events" ("id");

ALTER TABLE "artist_products" ADD FOREIGN KEY ("profile_id") REFERENCES "profiles" ("id");
