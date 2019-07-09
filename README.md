# bot-detector
## Prepare environment
Run `./prepare.sh` for prepare environment
* install Apache Flume
## Up environment
Run `docker-compose up` to up environment
Run `init.sh` to create click-stream topic (3 part)
Run `run-flume.sh` to start flume connector File => Kafka
## Initialize event generation
Run `./generate-events.sh`for generate events for stream (use botgen.py)