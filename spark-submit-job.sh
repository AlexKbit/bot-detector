#!/usr/bin/env bash

echo '===Spark-submit stream to master in cluster mode==='
docker exec -it bot-detector_spark-master_1 /spark/bin/spark-submit --class com.bot.detector.DetectorApp --master spark://spark-master:7077 --driver-memory 2g --deploy-mode cluster file:///etc/spark/apps/bot-detector.jar spark://spark-master:7077
echo '======Spark-submit successfully completed=========='

#spark-submit --class com.bot.detector.DetectorApp \
#             --master spark://localhost:7077 \
#             --deploy-mode client \
#             target/scala-2.11/bot-detector-0.1.jar spark://localhost:7077