package com.bot.detector

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.cassandra._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object DetectorDStreamApp {

  val DEFAULT_MASTER = "local[*]"
  val KAFKA_TOPIC = "click-stream"
  val BOT_BAN_TIME = 10 * 60 // 10 min

  val LIMIT_OF_EVENTS = 1000
  val LIMIT_OF_CATEGORIES = 5
  val LIMIT_OF_CLICK_VIEW = 0.6

  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "localhost:9092,localhost:9093,localhost:9094",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "dstream",
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (true: java.lang.Boolean)
  )

  def main(args: Array[String]): Unit = {
    val master = if (args.length == 0) DEFAULT_MASTER else args(0)
    val spark = createSparkSession(master)
    val streamContext = new StreamingContext(spark.sparkContext, Seconds(1))
    import spark.implicits._
    val stream = KafkaUtils.createDirectStream[String, String](
      streamContext,
      PreferConsistent,
      Subscribe[String, String](Array(KAFKA_TOPIC), kafkaParams)
    )
    val clickEvents = stream.map(rec => ClickEvent(rec.value()))

    clickEvents.window(Seconds(10), Seconds(10))
      .map(convert)
      .reduce(reduceAgg)
      .flatMap(m => m.values)
      .filter(a => a.eventsCount > LIMIT_OF_EVENTS
        || a.categories.size > LIMIT_OF_CATEGORIES
        || a.clickCount / a.viewCount > LIMIT_OF_CLICK_VIEW)
      .foreachRDD{ rdd =>
        //TODO
      }


    streamContext.awaitTermination()
  }

  def createSparkSession(master: String): SparkSession = {
    SparkSession.builder
      .master(master)
      .appName("Bot Detector")
      .config("spark.cassandra.connection.host", "localhost")
      .getOrCreate()
  }

  case class Aggregate(ip: String, eventsCount: Long, clickCount: Long, viewCount: Long, categories: Set[Int])

  case class MapAgg()
  case class MapAgg()

  def convert(clickEvent: ClickEvent): Map[String, Aggregate] = {
    val agg = Aggregate(clickEvent.ip, 1L,
      if (clickEvent.eventType == "click") 1 else 0,
      if (clickEvent.eventType == "view") 1 else 0,
      Set(clickEvent.categoryId))
    Map[String, Aggregate](clickEvent.ip -> agg)
  }

  def reduceAgg(agg1: Map[String, Aggregate], agg2: Map[String, Aggregate]): Map[String, Aggregate] = {
    val r = collection.mutable.Map[String, Aggregate]()
    for(k <- agg1.keySet.union(agg2.keySet)) {
      r.put(k, reduce(agg1.get(k), agg2.get(k)))
    }
    r.toMap
  }

  def reduce(agg1: Option[Aggregate], agg2: Option[Aggregate]): Aggregate = {
    if (agg1.isEmpty) {
      agg2.orNull
    } else {
      if (agg2.isEmpty) {
        agg1.orNull
      } else {
        val a1 = agg1.get
        val a2 = agg2.get
        Aggregate(a1.ip,
          a1.eventsCount + a2.eventsCount,
          a1.clickCount + a2.clickCount,
          a1.viewCount + a2.viewCount,
          a1.categories ++ a2.categories
        )
      }
    }
  }
}
