package com.bot.detector

import java.util.Date

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

case class ClickEvent(@JsonProperty("unix_time") time: Date,
                      @JsonProperty("category_id") categoryId: Int,
                      @JsonProperty("ip") ip: String,
                      @JsonProperty("type") eventType:String)

object ClickEvent {

  val mapper = new ObjectMapper() with ScalaObjectMapper

  def apply(value: String): ClickEvent = {
    mapper.readValue[ClickEvent](value)
  }
}
