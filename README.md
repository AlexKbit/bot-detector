# bot-detector
## Prepare environment
Run `./prepare.sh` for prepare environment
* install Apache Flume
## Up environment
Run `docker-compose up` to up environment
Run `run-flume.sh` to start flume connector File => Kafka
## Initialize event generation
Run `./generate-events.sh`for generate events for stream (use botgen.py)