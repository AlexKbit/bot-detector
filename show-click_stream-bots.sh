#!/usr/bin/env bash
docker exec -it bot-detector_cassandra1_1 /usr/bin/cqlsh -e "select * from botdetect.click_stream where is_bot = true;"