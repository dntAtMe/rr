package com.dntatme.recipes.api

import com.dntatme.recipes.api.RecipeApi
import com.dntatme.recipes.error.AppError
import com.dntatme.recipes.model.*
import com.dntatme.recipes.repository.*
import com.dntatme.recipes.service.RecipeService
import zio.*
import zio.http.*
import zio.test.*
import zio.test.Assertion.*

import java.sql.{SQLException, Timestamp}
import java.time.Instant
import javax.sql.DataSource

object RecipeApiSpec extends ZIOSpecDefault {

  private val testRecipe = Recipe(
    id = 1,
    title = "Pancakes",
    makingTime = 30,
    serves = 4,
    ingredients = "Flour, Eggs, Milk",
    cost = 10,
    createdAt = Timestamp.from(Instant.now()),
    updatedAt = Timestamp.from(Instant.now())
  )

  private val mockService: ULayer[RecipeService] = ZLayer.succeed(
    new RecipeService {
      override def createRecipe(recipeForm: RecipeForm): IO[AppError, Recipe] = {
        ZIO.succeed(testRecipe.copy(
          id = 1,
          title = recipeForm.title,
          makingTime = recipeForm.makingTime,
          serves = recipeForm.serves,
          ingredients = recipeForm.ingredients,
          cost = recipeForm.cost
        ))
      }

      override def getRecipe(id: Int): IO[AppError, Recipe] =
        if (id == testRecipe.id) ZIO.succeed(testRecipe)
        else ZIO.fail(AppError.NotFound("No recipe found"))

      override def getAllRecipes: IO[AppError, List[Recipe]] = ZIO.succeed(List(testRecipe))

      override def updateRecipe(id: Int, recipeForm: RecipeForm): IO[AppError, Recipe] =
        if (id == testRecipe.id) ZIO.succeed(
          testRecipe.copy(
            title = recipeForm.title,
            makingTime = recipeForm.makingTime,
            serves = recipeForm.serves,
            ingredients = recipeForm.ingredients,
            cost = recipeForm.cost,
            updatedAt = Timestamp.from(Instant.now())
          )
        )
        else ZIO.fail(AppError.NotFound("Recipe not found"))

      override def deleteRecipe(id: Int): IO[AppError, Unit] =
        if (id == testRecipe.id) ZIO.unit
        else ZIO.fail(AppError.NotFound("No recipe found"))
    }
  )


  override def spec: Spec[Any, Throwable] = suite("RecipeApiSpec")(
    test("POST /recipes should add a new recipe") {
      val request = Request(
        method = Method.POST,
        url = URL.root / "recipes",
        headers = Headers("Content-Type" -> "application/json"),
        body = Body.fromString(
          """
          {
            "title": "Pancakes",
            "makingTime": 30,
            "serves": 4,
            "ingredients": "Flour, Eggs, Milk",
            "cost": 10
          }
          """
        )
      )

      for {
        response <- RecipeApi.routes.runZIO(request)
        body <- response.body.asString
      } yield assert(response.status)(equalTo(Status.Ok)) &&
        assert(body)(containsString("Recipe successfully created!"))
    },
    test("POST /recipes should fail with incomplete body") {
      val request = Request(
        method = Method.POST,
        url = URL.root / "recipes",
        headers = Headers("Content-Type" -> "application/json"),
        body = Body.fromString(
          """
          {
            "title": "Pancakes",
            "serves": 4,
            "ingredients": "Flour, Eggs, Milk",
            "cost": 10
          }
          """
        )
      )

      for {
        response <- RecipeApi.routes.runZIO(request)
        body <- response.body.asString
      } yield assert(response.status)(equalTo(Status.BadRequest)) &&
        assert(body)(containsString("Recipe creation failed!"))
    },
    test("GET /recipes should return all recipes") {
      val request = Request(
        method = Method.GET,
        url = URL.root / "recipes"
      )

      for {
        response <- RecipeApi.routes.runZIO(request)
        body <- response.body.asString
      } yield assert(response.status)(equalTo(Status.Ok)) &&
        assert(body)(containsString("Pancakes"))
    },
    test("GET /recipes/{id} should return a recipe") {
      val request = Request(
        method = Method.GET,
        url = URL.root / "recipes" / "1"
      )

      for {
        response <- RecipeApi.routes.runZIO(request)
        body <- response.body.asString
      } yield assert(response.status)(equalTo(Status.Ok)) &&
        assert(body)(containsString("Pancakes"))
    },
    test("GET /recipes/{id} should return 404 if recipe not found") {
      val request = Request(
        method = Method.GET,
        url = URL.root / "recipes" / "99"
      )

      for {
        response <- RecipeApi.routes.runZIO(request)
        body <- response.body.asString
      } yield assert(response.status)(equalTo(Status.NotFound)) &&
        assert(body)(containsString("No recipe found"))
    },
    test("PATCH /recipes/{id} should update a recipe") {
      val request = Request(
        method = Method.PATCH,
        url = URL.root / "recipes" / "1",
        headers = Headers("Content-Type" -> "application/json"),
        body = Body.fromString(
          """
          {
            "title": "Updated Pancakes",
            "makingTime": 25,
            "serves": 2,
            "ingredients": "Flour, Eggs",
            "cost": 15
          }
          """
        )
      )

      for {
        response <- RecipeApi.routes.runZIO(request)
        body <- response.body.asString
      } yield assert(response.status)(equalTo(Status.Ok)) &&
        assert(body)(containsString("Recipe successfully updated"))
    },

    test("PATCH /recipes/{id} should fail with incomplete body") {
      val request = Request(
        method = Method.PATCH,
        url = URL.root / "recipes" / "1",
        headers = Headers("Content-Type" -> "application/json"),
        body = Body.fromString(
          """
          {
            "title": "Updated Pancakes",
            "serves": 2,
            "ingredients": "Flour, Eggs",
            "cost": 15
          }
          """
        )
      )

      for {
        response <- RecipeApi.routes.runZIO(request)
        body <- response.body.asString
      } yield assert(response.status)(equalTo(Status.BadRequest)) &&
        assert(body)(containsString("Recipe update failed!"))
    },
    test("DELETE /recipes/{id} should delete a recipe") {
      val request = Request(
        method = Method.DELETE,
        url = URL.root / "recipes" / "1"
      )

      for {
        response <- RecipeApi.routes.runZIO(request)
        body <- response.body.asString
      } yield assert(response.status)(equalTo(Status.Ok)) &&
        assert(body)(containsString("Recipe successfully removed!"))
    },
    test("DELETE /recipes/{id} should return 404 if recipe not found") {
      val request = Request(
        method = Method.DELETE,
        url = URL.root / "recipes" / "99"
      )

      for {
        response <- RecipeApi.routes.runZIO(request)
        body <- response.body.asString
      } yield assert(response.status)(equalTo(Status.NotFound)) &&
        assert(body)(containsString("No recipe found"))
    }
  ).provide(
    mockService
  )
}
