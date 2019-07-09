package com.bot.detector

import org.apache.spark.sql.{Dataset, Encoders, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.expressions.scalalang.typed
import org.apache.spark.sql.cassandra._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import com.datastax.spark.connector._
import com.datastax.spark.connector.streaming._

object DetectorApp {

  val DEFAULT_MASTER = "local[*]"

  def main(args: Array[String]): Unit = {
    val master = if (args.length == 0) DEFAULT_MASTER else args(0)
    val ss = createSparkSession(master)
    val r = ss.sparkContext.parallelize(1 to 1000).map(Math.pow(_, 2)).sum()
    println("Result: " + r)
  }

  def createSparkSession(master: String): SparkSession = {
    SparkSession.builder
      .master(master)
      .appName("Bot Detector")
      .config("spark.cassandra.connection.host", "localhost")
      //.enableHiveSupport
      .getOrCreate()
  }
}
