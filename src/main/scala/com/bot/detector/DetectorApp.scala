package com.bot.detector

import org.apache.spark.sql.{Dataset, Encoders, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.expressions.scalalang.typed
import org.apache.spark.sql.cassandra._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010._
import com.datastax.spark.connector._
import com.datastax.spark.connector.streaming._
import org.apache.spark.sql.streaming.OutputMode

object DetectorApp {

  val DEFAULT_MASTER = "local[*]"
  val KAFKA_TOPIC = "click-stream"
  val BOT_BAN_TIME = 10 * 60 // 10 min

  val LIMIT_OF_EVENTS = 1000
  val LIMIT_OF_CATEGORIES = 5
  val LIMIT_OF_CLICK_VIEW = 3

  def main(args: Array[String]): Unit = {
    val master = if (args.length == 0) DEFAULT_MASTER else args(0)
    val spark = createSparkSession(master)
    val streamContext = new StreamingContext(spark.sparkContext, Seconds(1))
    import spark.implicits._
    val clickStream = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094")
      .option("subscribe", KAFKA_TOPIC)
      .load()

    val clickEvents: Dataset[ClickEvent] = clickStream
      .selectExpr("CAST(value AS STRING)")
      .map(r => ClickEvent(r.getString(0)))

    val aggregates = clickEvents
      .withWatermark("time", "60 seconds")
      .groupBy(window($"time","10 minutes"), $"ip")
      .agg(
        count(col("eventType")).alias("eventsCount"),
        count(col("eventType") === "click").alias("clickCount"),
        count(col("eventType") === "view").alias("viewCount"),
        count(col("categoryId")).alias("categoryCount"))
      .where(s"eventsCount > $LIMIT_OF_EVENTS")
      .where(s"clickCount / viewCount > $LIMIT_OF_CLICK_VIEW")
      .where(s"categoryCount > $LIMIT_OF_CATEGORIES")

    aggregates
      .withColumn("ban_start", lit(current_timestamp()))
      .withColumnRenamed("eventsCount","events_count")
      .withColumnRenamed("clickCount","click_count")
      .withColumnRenamed("viewCount","view_count")
      .withColumnRenamed("categoryCount","category_count")
      .writeStream
      .option("checkpointLocation", "/tmp/check_point/")
      .foreachBatch { (batchDF, _) =>
        batchDF
          .write
          .cassandraFormat("detected_bots", "botdetect")
          .option("spark.cassandra.output.ttl", BOT_BAN_TIME)
          .mode(SaveMode.Append)
          .save
       }.start

    //streamContext.start()
    streamContext.awaitTermination()
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
