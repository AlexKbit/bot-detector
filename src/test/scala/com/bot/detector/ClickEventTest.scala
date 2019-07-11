package com.bot.detector

import org.scalatest.FlatSpec

class ClickEventTest extends FlatSpec {

  "click stream" should "parsed to ClickEvent" in {
        val jsonStr = "{\"unix_time\": 1562828422, \"category_id\": 1000, \"ip\": \"172.10.0.57\", \"type\": \"click\"}"
        val event = ClickEvent(jsonStr)
        assert(event.ip === "172.10.0.57")
        assert(event.categoryId === 1000)
        assert(event.eventType === "click")
  }
}
