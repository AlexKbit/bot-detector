package com.bot.detector

import java.util.Date

import com.fasterxml.jackson.annotation.JsonProperty

case class ClickEvent(@JsonProperty("unix_time") time: Date,
                      @JsonProperty("category_id") categoryId: Int,
                      @JsonProperty("ip") ip: String,
                      @JsonProperty("type") eventType:String)
