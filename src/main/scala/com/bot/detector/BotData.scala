package com.bot.detector

import java.sql.Timestamp

case class BotData(ip: String, detectionTime: Timestamp = new Timestamp())
