CREATE KEYSPACE botdetect WITH replication = {'class':'SimpleStrategy','replication_factor':1};
CREATE TABLE botdetect.detected_bots (
    ip text,
    window bigint,
    events_count int,
    click_count int,
    view_count int,
    category_count int,
    ban_start timestamp,
    PRIMARY KEY(ip));
