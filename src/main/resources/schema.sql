CREATE TABLE blog
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    blog_title  VARCHAR(255)          NOT NULL,
    description CLOB                  NOT NULL,
    CONSTRAINT pk_blog PRIMARY KEY (id)
);

CREATE TABLE post
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    blog_id         BIGINT,
    post_title      VARCHAR(255)          NOT NULL,
    post_body       CLOB                  NOT NULL,
    post_conclusion CLOB,
    author          VARCHAR(40)           NOT NULL,
    published_on    date                  NOT NULL,
    CONSTRAINT pk_post PRIMARY KEY (id)
);

ALTER TABLE post
    ADD CONSTRAINT FK_POST_ON_BLOG FOREIGN KEY (blog_id) REFERENCES blog (id);

CREATE ALIAS IF NOT EXISTS FT_INIT FOR "org.h2.fulltext.FullText.init";
CALL FT_INIT();
CALL FT_CREATE_INDEX('PUBLIC', 'POST', 'POST_TITLE,POST_BODY');

