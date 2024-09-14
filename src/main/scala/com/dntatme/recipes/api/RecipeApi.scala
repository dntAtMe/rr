package com.dntatme.recipes.api

import com.dntatme.recipes.api.ResponseContent.{
  ErrorFormContent,
  MessageContent,
  RecipeContent,
  RecipesContent
}
import com.dntatme.recipes.error.AppError
import com.dntatme.recipes.model.*
import com.dntatme.recipes.service.RecipeService
import zio.*
import zio.http.*
import zio.json.*

object RecipeApi {

  private def errorHandler: Throwable => Response =
    case e: AppError.InvalidInput =>
      Response
        .json(ErrorFormContent(e.message, RecipeForm.mkFields).toJson)
        .status(Status.BadRequest)
    case e: AppError.NotFound =>
      Response.json(MessageContent(e.message).toJson).status(Status.NotFound)
    case e =>
      Response
        .json(MessageContent("Internal server error").toJson)
        .status(Status.InternalServerError)

  private def createRecipeHandler(
      req: Request
  ): ZIO[RecipeService, Throwable, Response] =
    for {
      service <- ZIO.service[RecipeService]
      body <- req.body.asString
      form <- ZIO
        .fromEither(body.fromJson[RecipeForm])
        .mapError(e => AppError.InvalidInput("Recipe creation failed!"))
      recipe <- service.createRecipe(form)
      response <- ZIO.succeed(
        Response.json(
          RecipeContent("Recipe successfully created!", recipe :: Nil).toJson
        )
      )
    } yield response

  private def getAllRecipesHandler(
      req: Request
  ): ZIO[RecipeService, AppError, Response] =
    for {
      service <- ZIO.service[RecipeService]
      recipes <- service.getAllRecipes
      response <- ZIO.succeed(Response.json(RecipesContent(recipes).toJson))
    } yield response

  private def getRecipeByIdHandler(
      id: Int,
      req: Request
  ): ZIO[RecipeService, AppError, Response] =
    for {
      service <- ZIO.service[RecipeService]
      recipe <- service.getRecipe(id)
      response <- ZIO.succeed(
        Response.json(RecipeContent("Recipe details by id", recipe :: Nil).toJson)
      )
    } yield response

  private def updateRecipeHandler(
      id: Int,
      req: Request
  ): ZIO[RecipeService, Throwable, Response] =
    for {
      service <- ZIO.service[RecipeService]
      body <- req.body.asString
      form <- ZIO
        .fromEither(body.fromJson[RecipeForm])
        .mapError(e => AppError.InvalidInput("Recipe update failed!"))
      recipe <- service.updateRecipe(id, form)
      response <- ZIO.succeed(
        Response.json(RecipeContent("Recipe successfully updated!", recipe :: Nil).toJson)
      )
    } yield response

  private def deleteRecipeHandler(
      id: Int,
      req: Request
  ): ZIO[RecipeService, AppError, Response] =
    for {
      service <- ZIO.service[RecipeService]
      deleted <- service.deleteRecipe(id)
      response <- ZIO.succeed(Response.json(MessageContent("Recipe successfully removed!").toJson))
    } yield response

  def routes: Routes[RecipeService, Nothing] = Routes(
    Method.POST / "recipes" -> handler(createRecipeHandler),
    Method.GET / "recipes" -> handler(getAllRecipesHandler),
    Method.GET / "recipes" / int("id") -> handler(getRecipeByIdHandler),
    Method.PATCH / "recipes" / int("id") -> handler(updateRecipeHandler),
    Method.DELETE / "recipes" / int("id") -> handler(deleteRecipeHandler)
  ).handleError(errorHandler)
}
