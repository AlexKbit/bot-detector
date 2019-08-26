package com.bot.detector

import java.sql.Timestamp

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.cassandra._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.OutputMode
import com.datastax.spark.connector._
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object DetectorDStreamApp {

  val defaultMaster = "local[*]"
  val kafkaTopic = "click-stream"

  val limitOfEvents = 20

  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "localhost:9092,localhost:9093,localhost:9094",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "dstream",
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (true: java.lang.Boolean)
  )

  def main(args: Array[String]): Unit = {
    val master = if (args.length == 0) defaultMaster else args(0)
    val spark = CommonUtil.createSparkSession(master)
    val streamContext = new StreamingContext(spark.sparkContext, Seconds(1))
    import spark.implicits._
    val stream = KafkaUtils.createDirectStream[String, String](
      streamContext,
      PreferConsistent,
      Subscribe[String, String](Array(kafkaTopic), kafkaParams)
    )
    val clickEvents = stream.map(rec => ClickEvent(rec.value()))

    clickEvents.foreachRDD { rdd =>
      rdd.map(event => (event.eventType, event.ip, RedisUtil.isBot(event.ip)(spark), event.time, event.categoryId))
        .saveToCassandra("botdetect", "click_stream", SomeColumns("type", "ip", "is_bot", "time", "category_id"))
    }

    clickEvents
      .window(Seconds(10), Seconds(10))
      .filter(event => event.time.getTime < System.currentTimeMillis() + 10 * 60 * 1000)
      .map(convert)
      .reduce(reduceAgg)
      .flatMap(m => m.values)
      .filter(a => a.eventsCount > limitOfEvents)
      .foreachRDD{ rdd =>
        RedisUtil.writeBotsRdd(rdd.map(agg => BotData(agg.ip)))(spark)
      }

    streamContext.awaitTermination()
  }

  case class Aggregate(ip: String, eventsCount: Long)

  def convert(clickEvent: ClickEvent): Map[String, Aggregate] = {
    val agg = Aggregate(clickEvent.ip, 1L)
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
        Aggregate(a1.ip, a1.eventsCount + a2.eventsCount)
      }
    }
  }
}
