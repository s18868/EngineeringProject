ALTER TABLE "user" DROP COLUMN name, DROP COLUMN surname;

ALTER TABLE "user" ADD COLUMN profile_id BIGINT NOT NULL;

ALTER TABLE "user" ADD FOREIGN KEY(profile_id) REFERENCES profile;