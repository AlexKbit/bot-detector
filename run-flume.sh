#!/bin/bash
echo 'Run Flume pipeline'
flume-ng agent -n tier1 -c conf -f flume/flume.conf -Xms1024m -Xmx1024m