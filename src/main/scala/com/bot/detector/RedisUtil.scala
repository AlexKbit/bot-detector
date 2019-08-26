package com.bot.detector

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}

object RedisUtil {

  val redisTable = "bots"
  val keyField = "ip"
  val botBanTime = 10 * 60 // 10 min

  def writeBotsRdd(bots: RDD[BotData])(implicit sparkSession: SparkSession): Unit = {
    import sparkSession.implicits._
    writeBotsDF(bots.toDF())
  }

  def writeBotsDS(bots: Dataset[BotData])(implicit sparkSession: SparkSession): Unit = {
    import sparkSession.implicits._
    writeBotsDF(bots.toDF())
  }

  def writeBotsDF(bots: DataFrame): Unit = {
    bots.write
      .format("org.apache.spark.sql.redis")
      .option("table", redisTable)
      .option("key.column", keyField)
      .option("ttl", botBanTime)
      .mode(SaveMode.Append)
      .save()
  }

  def readBots(implicit sparkSession: SparkSession) = {
    sparkSession.read
      .format("org.apache.spark.sql.redis")
      .option("table", redisTable)
      .option("key.column", keyField)
      .load()
  }

  def isBot(ip: String)(implicit sparkSession: SparkSession): Boolean = {
    readBots.filter(r => r.getAs(keyField) == ip).count() > 0
  }

  def isBot(bots: DataFrame, ip: String)(implicit sparkSession: SparkSession): Boolean = {
    bots.filter(r => r.getAs(keyField) == ip).count() > 0
  }
}
