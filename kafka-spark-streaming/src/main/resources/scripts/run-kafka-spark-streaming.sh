#!/usr/bin/env bash

###############################################################################
#  this is an example script the will require edits to make it work in any
#  environment.
###############################################################################
## THIS IS CLUSTER/PLATFORM DEPENDENT
# MapR
# https://community.mapr.com/docs/DOC-1396
SPLICE_LIB_DIR="/opt/cloudera/parcels/SPLICEMACHINE/lib"
KAFKA_LIB_DIR="/opt/kafka/libs"
export SPARK_HOME="/opt/cloudera/parcels/CDH/lib/spark"
export HADOOP_CONF_DIR=/opt/mapr/hadoop/hadoop-2.7.0/etc/hadoop/
export HADOOP_CONF_DIR=/etc/hadoop/conf

# Cloudera
# SPLICE_LIB_DIR="/opt/cloudera/parcels/SPLICEMACHINE/lib"
# KAFKA_LIB_DIR="/opt/cloudera/parcels/KAFKA/lib/kafka/libs/"
# export SPARK_HOME="/opt/cloudera/parcels/CDH/lib/spark"
# export HADOOP_CONF_DIR=/etc/hadoop/conf


CLASS_NAME="com.splicemachine.tutorials.sparkstreaming.kafka.SparkStreamingKafka"
export ADDITIONAL_JARS="${SPLICE_LIB_DIR}/spark-streaming-kafka*.jar,${KAFKA_LIB_DIR}/kafka-clients-*.jar"
export APPLICATION_JAR="${SPLICE_LIB_DIR}/splice-tutorial-kafka-spark-streaming-2.0.1.18.jar"

exec "${SPARK_HOME}"/bin/spark-submit \
  --name TutorialKafka \
  --master yarn \
  --deploy-mode cluster \
  --executor-memory 2G \
  --num-executors 3 \
  --class ${CLASS_NAME} \
  --jars ${ADDITIONAL_JARS} \
  "${APPLICATION_JAR}" \
  "$@"