CREATE TABLE "follows" (
  "follower_profile_id" VARCHAR(36) NOT NULL,
  "following_profile_id" VARCHAR(36) NOT NULL,
  "created_at" TIMESTAMP NOT NULL,
  PRIMARY KEY ("follower_profile_id", "following_profile_id")
);

CREATE TABLE "profiles" (
  "id" VARCHAR(36) PRIMARY KEY NOT NULL,
  "username" VARCHAR(50) UNIQUE,
  "name" VARCHAR(150),
  "email" VARCHAR(255) UNIQUE NOT NULL,
  "password" VARCHAR(255) NOT NULL,
  "public_email" VARCHAR(255) DEFAULT NULL,
  "type" VARCHAR(50) NOT NULL CHECK ("type" IN ('user', 'organization')),
  "followers_count" INTEGER DEFAULT 0,
  "following_count" INTEGER DEFAULT 0,
  "created_at" TIMESTAMP NOT NULL,
  "updated_at" TIMESTAMP DEFAULT NULL,
  "deleted_at" TIMESTAMP DEFAULT NULL
);

CREATE TABLE "posts" (
  "id" VARCHAR(36) PRIMARY KEY NOT NULL,
  "profile_id" VARCHAR(36) NOT NULL,
  "img_link" VARCHAR(255) DEFAULT NULL,
  "description" TEXT,
  "likes_count" INTEGER,
  "created_at" TIMESTAMP NOT NULL,
  "deleted_at" TIMESTAMP DEFAULT NULL
);

CREATE TABLE "events" (
  "id" VARCHAR(36) PRIMARY KEY NOT NULL,
  "profile_id" VARCHAR(36) NOT NULL,
  "title" VARCHAR(255) NOT NULL,
  "img_link" VARCHAR(255),
  "event_time" TIMESTAMP NOT NULL,
  "description" TEXT,
  "street_address" VARCHAR(150),
  "administrative_region" VARCHAR(150),
  "city" VARCHAR(150),
  "state" VARCHAR(150),
  "reference_point" VARCHAR(255),
  "latitude" DECIMAL(10,6),
  "longitude" DECIMAL(11,6),
  "created_at" TIMESTAMP NOT NULL,
  "finished_at" TIMESTAMP
);

CREATE TABLE "likes" (
  "user_id" VARCHAR(36),
  "post_id" VARCHAR(36),
  "created_at" TIMESTAMP NOT NULL,
  PRIMARY KEY ("user_id", "post_id")
);

CREATE TABLE "shareds" (
  "id" VARCHAR(36) PRIMARY KEY NOT NULL,
  "profile_sharing_id" VARCHAR(36) NOT NULL,
  "profile_shared_id" VARCHAR(36) NOT NULL,
  "post_id" VARCHAR(36),
  "created_at" TIMESTAMP NOT NULL
);

CREATE TABLE "memberships" (
  "id" VARCHAR(36) PRIMARY KEY NOT NULL,
  "profile_id" VARCHAR(36) NOT NULL,
  "organization_id" VARCHAR(36) NOT NULL,
  "role" VARCHAR DEFAULT 'member',
  "created_at" TIMESTAMP NOT NULL
);

CREATE TABLE "event_participants" (
  "profile_id" VARCHAR(36),
  "event_id" VARCHAR(36),
  "created_at" TIMESTAMP NOT NULL,
  PRIMARY KEY ("profile_id", "event_id")
);

CREATE TABLE "artist_products" (
  "id" VARCHAR(36) PRIMARY KEY NOT NULL,
  "profile_id" VARCHAR(36) NOT NULL,
  "title" VARCHAR(255) NOT NULL,
  "img_link" VARCHAR(255),
  "price" DECIMAL(10,2) NOT NULL,
  "created_at" TIMESTAMP NOT NULL,
  "deleted_at" TIMESTAMP
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
