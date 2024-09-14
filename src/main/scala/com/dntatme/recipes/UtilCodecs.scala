package com.dntatme.recipes

import zio.json.{JsonDecoder, JsonEncoder}

import java.sql.Timestamp

object UtilCodecs:
  given timestampEncoder: JsonEncoder[Timestamp] =
    JsonEncoder[Long].contramap(_.getTime)

  given timestampDecoder: JsonDecoder[Timestamp] =
    JsonDecoder[Long].map(new Timestamp(_))
