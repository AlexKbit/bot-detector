#!/bin/bash
echo 'Generate click-events to [flume/data/data-RANDOM.json]'
python botgen.py -f flume/data/data-$RANDOM.json -b 10 -u 1000 -n 100 -d 300