CREATE KEYSPACE botdetect WITH replication = {'class':'SimpleStrategy','replication_factor':1};
CREATE TABLE botdetect.detected_bots (
    detection_id text,
    average_events double,
    total_events double,
    max_events double,
    min_events double,
    standard_deviation_amount double,
    bot_ip text,
    PRIMARY KEY(detection_id));
