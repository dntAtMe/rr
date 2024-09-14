package com.dntatme.recipes.api

import zio.json.{DeriveJsonEncoder, JsonEncoder}

sealed trait ResponseContent

object ResponseContent {

  import com.dntatme.recipes.model.Recipe

  implicit val messageResponseEncoder: JsonEncoder[MessageContent] =
    DeriveJsonEncoder.gen[MessageContent]
  implicit val recipeResponseEncoder: JsonEncoder[RecipeContent] =
    DeriveJsonEncoder.gen[RecipeContent]
  implicit val recipesResponseEncoder: JsonEncoder[RecipesContent] =
    DeriveJsonEncoder.gen[RecipesContent]
  implicit val errorFormResponseEncoder: JsonEncoder[ErrorFormContent] =
    DeriveJsonEncoder.gen[ErrorFormContent]

  implicit val responseContentEncoder: JsonEncoder[ResponseContent] =
    DeriveJsonEncoder.gen[ResponseContent]

  final case class MessageContent(message: String) extends ResponseContent
  final case class RecipeContent(message: String, recipe: Seq[Recipe]) extends ResponseContent
  final case class RecipesContent(recipes: Seq[Recipe]) extends ResponseContent
  final case class ErrorFormContent(message: String, required: String) extends ResponseContent
}
