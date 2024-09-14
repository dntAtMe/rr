package com.dntatme.recipes.model

import zio.json._
import scala.deriving.Mirror
import scala.compiletime.constValueTuple

case class RecipeForm(
    title: String,
    makingTime: Int,
    serves: Int,
    ingredients: String,
    cost: Int
)

object RecipeForm:
  given decoder: JsonDecoder[RecipeForm] = DeriveJsonDecoder.gen[RecipeForm]
  given encoder: JsonEncoder[RecipeForm] = DeriveJsonEncoder.gen[RecipeForm]

  inline def mkFields(using m: Mirror.ProductOf[RecipeForm]): String =
    val labels = getElemLabels
    labels.mkString(", ")

  inline def getElemLabels(using m: Mirror.ProductOf[RecipeForm]): List[String] =
    inline m match
      case s: Mirror.ProductOf[RecipeForm] =>
        constValueTuple[s.MirroredElemLabels].toList.asInstanceOf[List[String]]
