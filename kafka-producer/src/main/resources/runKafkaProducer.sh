#!/usr/bin/env bash

###############################################################################
#  this is an example script the will require edits to make it work in any
#  environment.
###############################################################################
HOST="srv126"
## THIS IS CLUSTER/PLATFORM DEPENDENT
# MapR or HDP
SPLICE_LIB_DIR="/opt/cloudera/parcels/SPLICEMACHINE/lib"
KAFKA_LIB_DIR="/opt/kafka/libs"
# Cloudera
# SPLICE_LIB_DIR="/opt/cloudera/parcels/SPLICEMACHINE/lib"
# KAFKA_LIB_DIR="/opt/cloudera/parcels/KAFKA/lib/kafka/libs/"

SPLICE_JAR="${SPLICE_LIB_DIR}/kafka-producer-2.0.jar"
APPENDSTRING=`echo ${KAFKA_LIB_DIR}/*.jar | sed 's/ /:/g'`

java -cp ${SPLICE_JAR}:${APPENDSTRING} \
    -Dlog4j.configuration=file:///tmp/log4j.properties \
    com.splicemachine.tutorials.sparkstreaming.kafka.KafkaTopicProducer \
    ${HOST}:9092 $@
