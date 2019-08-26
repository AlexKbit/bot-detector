CREATE KEYSPACE botdetect WITH replication = {'class':'SimpleStrategy','replication_factor':1};
CREATE TABLE botdetect.click_stream (
    type text,
    ip text,
    is_bot boolean,
    time bigint,
    category_id int);
