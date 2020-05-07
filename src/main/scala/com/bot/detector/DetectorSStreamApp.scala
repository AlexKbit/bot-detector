package com.bot.detector

import java.util.UUID

import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.cassandra._
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.streaming._

object DetectorSStreamApp {

  val defaultMaster = "local[*]"
  val kafkaTopic = "click-stream"

  val limitOfEvents = 20

  def main(args: Array[String]): Unit = {
    val master = if (args.length == 0) defaultMaster else args(0)
    val spark = CommonUtil.createSparkSession(master)
    val streamContext = new StreamingContext(spark.sparkContext, Seconds(1))
    import spark.implicits._
    val clickStream = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094")
      .option("subscribe", kafkaTopic)
      .load()

    val clickEvents: Dataset[ClickEvent] = clickStream
      .selectExpr("CAST(value AS STRING)")
      .map(r => ClickEvent(r.getString(0)))

    clickEvents
      .writeStream
      .foreachBatch { (batch, _) =>
        val bots = RedisUtil.readBots(spark).cache()
        batch.map(event => (UUID.randomUUID().toString, event.eventType, event.ip, RedisUtil.isBot(bots, event.ip)(spark), event.time, event.categoryId))
          .write
          .cassandraFormat("click_stream", "botdetect")
          .mode(SaveMode.Append)
          .save
    }

    val aggregates = clickEvents
      .withWatermark("time", "10 minutes")
      .groupBy(window($"time", "10 seconds", "10 seconds"), $"ip")
      .agg(count(col("eventType")).alias("eventsCount"))
      .where(col("eventsCount") > limitOfEvents)

    aggregates
      .writeStream
      .outputMode(OutputMode.Append)
      .option("checkpointLocation", "/tmp/check_point/")
      .foreachBatch { (batchDF, _) =>
        RedisUtil.writeBotsDS(batchDF.map(r => BotData(r.getAs("ip").toString)))(spark)
       }.start

    streamContext.awaitTermination()
  }

  def createSparkSession(master: String): SparkSession = {
    SparkSession.builder
      .master(master)
      .appName("Bot Detector")
      .config("spark.cassandra.connection.host", "localhost")
      .getOrCreate()
  }
}
