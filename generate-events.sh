#!/bin/bash
echo 'Generate click-events to [flume/data/data-RANDOM.json]'
python botgen.py -f flume/data/data-$RANDOM.json -b 20 -u 1000 -n 1000 -d 300