/*
 Navicat Premium Dump SQL

 Source Server         : PostgreSQL
 Source Server Type    : PostgreSQL
 Source Server Version : 170005 (170005)
 Source Host           : localhost:5432
 Source Catalog        : awachatnext
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 170005 (170005)
 File Encoding         : 65001

 Date: 05/06/2025 20:58:38
*/


-- ----------------------------
-- Sequence structure for private_chat_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."private_chat_seq";
CREATE SEQUENCE "public"."private_chat_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 999999999999
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for private_message_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."private_message_seq";
CREATE SEQUENCE "public"."private_message_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 99999999999999
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for userid_sequence
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."userid_sequence";
CREATE SEQUENCE "public"."userid_sequence" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Table structure for friend
-- ----------------------------
DROP TABLE IF EXISTS "public"."friend";
CREATE TABLE "public"."friend" (
  "user1" int4 NOT NULL,
  "user2" int4 NOT NULL
)
;
COMMENT ON COLUMN "public"."friend"."user1" IS '用户1 ID';
COMMENT ON COLUMN "public"."friend"."user2" IS '用户2 ID';

-- ----------------------------
-- Table structure for private_chat
-- ----------------------------
DROP TABLE IF EXISTS "public"."private_chat";
CREATE TABLE "public"."private_chat" (
  "chat_id" int8 NOT NULL DEFAULT nextval('private_chat_seq'::regclass),
  "user1_id" int4 NOT NULL,
  "user2_id" int4 NOT NULL,
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "updated_at" timestamptz(6) NOT NULL DEFAULT now(),
  "last_message_id" int8
)
;
COMMENT ON COLUMN "public"."private_chat"."chat_id" IS '会话ID';
COMMENT ON COLUMN "public"."private_chat"."user1_id" IS '用户1 ID';
COMMENT ON COLUMN "public"."private_chat"."user2_id" IS '用户2 ID';
COMMENT ON COLUMN "public"."private_chat"."created_at" IS '会话创建时间（有默认值）';
COMMENT ON COLUMN "public"."private_chat"."updated_at" IS '最后活动时间（有默认值）';
COMMENT ON COLUMN "public"."private_chat"."last_message_id" IS '最后一条消息ID';

-- ----------------------------
-- Table structure for private_message
-- ----------------------------
DROP TABLE IF EXISTS "public"."private_message";
CREATE TABLE "public"."private_message" (
  "message_id" int8 NOT NULL DEFAULT nextval('private_message_seq'::regclass),
  "chat_id" int8 NOT NULL,
  "sender_id" int4 NOT NULL,
  "receiver_id" int4 NOT NULL,
  "content" jsonb NOT NULL,
  "content_type" int2 NOT NULL,
  "reply_to" int8,
  "sent_at" timestamptz(6) NOT NULL DEFAULT now(),
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "public"."private_message"."message_id" IS '消息ID';
COMMENT ON COLUMN "public"."private_message"."chat_id" IS '会话ID';
COMMENT ON COLUMN "public"."private_message"."sender_id" IS '发送者ID';
COMMENT ON COLUMN "public"."private_message"."receiver_id" IS '接收者ID';
COMMENT ON COLUMN "public"."private_message"."content" IS '消息内容';
COMMENT ON COLUMN "public"."private_message"."content_type" IS '消息类型';
COMMENT ON COLUMN "public"."private_message"."reply_to" IS '回复的消息';
COMMENT ON COLUMN "public"."private_message"."sent_at" IS '发送时间（有默认值）';
COMMENT ON COLUMN "public"."private_message"."is_deleted" IS '是否已标记删除（有默认值）';

-- ----------------------------
-- Table structure for private_message_acknowledge
-- ----------------------------
DROP TABLE IF EXISTS "public"."private_message_acknowledge";
CREATE TABLE "public"."private_message_acknowledge" (
  "chat_id" int8 NOT NULL,
  "user_id" int4 NOT NULL,
  "last_message_id" int8 NOT NULL
)
;
COMMENT ON COLUMN "public"."private_message_acknowledge"."chat_id" IS '会话ID';
COMMENT ON COLUMN "public"."private_message_acknowledge"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."private_message_acknowledge"."last_message_id" IS '最后已读消息ID';

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS "public"."user";
CREATE TABLE "public"."user" (
  "user_id" int4 NOT NULL DEFAULT nextval('userid_sequence'::regclass),
  "username" text COLLATE "pg_catalog"."default" NOT NULL,
  "nickname" text COLLATE "pg_catalog"."default" NOT NULL,
  "password" text COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::text,
  "avatar" text COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT now(),
  "last_online_at" timestamptz(6),
  "role" int2 NOT NULL DEFAULT 0,
  "ban_until" timestamptz(6),
  "extended_data" jsonb,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "public"."user"."user_id" IS '用户ID';
COMMENT ON COLUMN "public"."user"."username" IS '用户登录名';
COMMENT ON COLUMN "public"."user"."nickname" IS '用户昵称';
COMMENT ON COLUMN "public"."user"."password" IS '加密后的密码';
COMMENT ON COLUMN "public"."user"."description" IS '用户描述（有默认值）';
COMMENT ON COLUMN "public"."user"."avatar" IS '头像图片id，空表示没有头像';
COMMENT ON COLUMN "public"."user"."created_at" IS '注册时间（有默认值）';
COMMENT ON COLUMN "public"."user"."last_online_at" IS '上次登录时间';
COMMENT ON COLUMN "public"."user"."role" IS '权限（有默认值）';
COMMENT ON COLUMN "public"."user"."ban_until" IS '解封时间';
COMMENT ON COLUMN "public"."user"."extended_data" IS '扩展信息';
COMMENT ON COLUMN "public"."user"."is_deleted" IS '是否已标记删除（有默认值）';

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."private_chat_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."private_message_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."userid_sequence"', 6, true);

-- ----------------------------
-- Primary Key structure for table friend
-- ----------------------------
ALTER TABLE "public"."friend" ADD CONSTRAINT "friend_pkey" PRIMARY KEY ("user1", "user2");

-- ----------------------------
-- Primary Key structure for table private_chat
-- ----------------------------
ALTER TABLE "public"."private_chat" ADD CONSTRAINT "private_chat_pkey" PRIMARY KEY ("chat_id");

-- ----------------------------
-- Primary Key structure for table private_message
-- ----------------------------
ALTER TABLE "public"."private_message" ADD CONSTRAINT "private_message_pkey" PRIMARY KEY ("message_id");

-- ----------------------------
-- Primary Key structure for table private_message_acknowledge
-- ----------------------------
ALTER TABLE "public"."private_message_acknowledge" ADD CONSTRAINT "private_message_acknowledge_pkey" PRIMARY KEY ("chat_id", "user_id");

-- ----------------------------
-- Primary Key structure for table user
-- ----------------------------
ALTER TABLE "public"."user" ADD CONSTRAINT "user_pkey" PRIMARY KEY ("user_id");

-- ----------------------------
-- Foreign Keys structure for table friend
-- ----------------------------
ALTER TABLE "public"."friend" ADD CONSTRAINT "friend_user1" FOREIGN KEY ("user1") REFERENCES "public"."user" ("user_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."friend" ADD CONSTRAINT "friend_user2" FOREIGN KEY ("user2") REFERENCES "public"."user" ("user_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table private_chat
-- ----------------------------
ALTER TABLE "public"."private_chat" ADD CONSTRAINT "privchat_last_message" FOREIGN KEY ("last_message_id") REFERENCES "public"."private_message" ("message_id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."private_chat" ADD CONSTRAINT "privchat_user1_id" FOREIGN KEY ("user1_id") REFERENCES "public"."user" ("user_id") ON DELETE RESTRICT ON UPDATE NO ACTION;
ALTER TABLE "public"."private_chat" ADD CONSTRAINT "privchat_user2_id" FOREIGN KEY ("user2_id") REFERENCES "public"."user" ("user_id") ON DELETE RESTRICT ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table private_message
-- ----------------------------
ALTER TABLE "public"."private_message" ADD CONSTRAINT "priv_msg_chat_id" FOREIGN KEY ("chat_id") REFERENCES "public"."private_chat" ("chat_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."private_message" ADD CONSTRAINT "priv_msg_receiver_id" FOREIGN KEY ("receiver_id") REFERENCES "public"."user" ("user_id") ON DELETE RESTRICT ON UPDATE NO ACTION;
ALTER TABLE "public"."private_message" ADD CONSTRAINT "priv_msg_reply_to" FOREIGN KEY ("reply_to") REFERENCES "public"."private_message" ("message_id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."private_message" ADD CONSTRAINT "priv_msg_sender_id" FOREIGN KEY ("sender_id") REFERENCES "public"."user" ("user_id") ON DELETE RESTRICT ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table private_message_acknowledge
-- ----------------------------
ALTER TABLE "public"."private_message_acknowledge" ADD CONSTRAINT "last_message_id" FOREIGN KEY ("last_message_id") REFERENCES "public"."private_message" ("message_id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."private_message_acknowledge" ADD CONSTRAINT "private_ack_chat_id" FOREIGN KEY ("chat_id") REFERENCES "public"."private_chat" ("chat_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."private_message_acknowledge" ADD CONSTRAINT "private_ack_user_id" FOREIGN KEY ("user_id") REFERENCES "public"."user" ("user_id") ON DELETE CASCADE ON UPDATE CASCADE;
COMMENT ON CONSTRAINT "last_message_id" ON "public"."private_message_acknowledge" IS '仅用于比较大小。原消息可能不存在';
