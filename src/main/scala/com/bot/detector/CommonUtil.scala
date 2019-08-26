package com.bot.detector

import org.apache.spark.sql.SparkSession

object CommonUtil {

  def createSparkSession(master: String): SparkSession = {
    SparkSession.builder
      .master(master)
      .appName("Bot Detector")
      .config("spark.cassandra.connection.host", "localhost")
      .config("redis.host", "localhost")
      .config("redis.port", "6379")
      .getOrCreate()
  }
}
