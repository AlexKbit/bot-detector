#!/bin/bash
echo '=========================Start initialize script====================='
echo '[Kafka prepare]'
echo 'Create kafka-topic:[click-stream] partitions: 3 replication-factor: 1'
docker exec -it bot-detector_kafka1_1 /usr/bin/kafka-topics --create --topic click-stream --partitions 3 --replication-factor 1 --zookeeper zoo1:2181

echo '[Cassandra prepare]'
echo 'Copy create.sql to Cassandra'
docker cp cassandra/create.sql bot-detector_cassandra1_1:/create.sql
echo 'Create Cassandra namespace[botdetect] and table[click_stream]'
docker exec -it bot-detector_cassandra1_1 /usr/bin/cqlsh -f /create.sql

echo '[Spark streams prepare]'
echo 'Run sbt assembly'
sbt assembly
echo 'Build completed'
echo 'Push bot-detector to [spark/apps]'
docker cp target/scala-2.11/bot-detector-0.1.jar bot-detector_spark-master_1:/etc/spark/apps/bot-detector.jar
echo '================Initialize script successfully completed=============='