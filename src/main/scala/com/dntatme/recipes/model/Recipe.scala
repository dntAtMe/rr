package com.dntatme.recipes.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.sql.Timestamp

case class Recipe(
    id: Int,
    title: String,
    makingTime: Int,
    serves: Int,
    ingredients: String,
    cost: Int,
    createdAt: Timestamp,
    updatedAt: Timestamp
)

object Recipe:
  import com.dntatme.recipes.UtilCodecs.given

  given recipeDecoder: JsonDecoder[Recipe] = DeriveJsonDecoder.gen[Recipe]
  given recipeEncoder: JsonEncoder[Recipe] = DeriveJsonEncoder.gen[Recipe]
