create TABLE file_check (
   file_key VARCHAR NOT NULL,
   chat_id VARCHAR NOT NULL,
   count BIGINT DEFAULT NULL,
   CONSTRAINT pk_file_check PRIMARY KEY (file_key)
);

create TABLE review_check (
   id VARCHAR NOT NULL,
   text VARCHAR NOT NULL,
   is_fake BOOLEAN,
   fake_score_percentage FLOAT,
   file_id VARCHAR,
   CONSTRAINT pk_review_check PRIMARY KEY (id)
);

alter table review_check add CONSTRAINT FK_REVIEW_CHECK_ON_FILE FOREIGN KEY (file_id) REFERENCES file_check (file_key);

