package com.dntatme.recipes.service

import zio._
import zio.test._
import zio.test.Assertion._
import com.dntatme.recipes.model._
import com.dntatme.recipes.repository.RecipeRepository
import com.dntatme.recipes.error.AppError
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant

object RecipeServiceImplSpec extends ZIOSpecDefault {
  
  private val testRecipe = Recipe(
    id = 1,
    title = "Test Recipe",
    makingTime = 30,
    serves = 4,
    ingredients = "Test ingredients",
    cost = 10,
    createdAt = Timestamp.from(Instant.now()),
    updatedAt = Timestamp.from(Instant.now())
  )

  private val testRecipes = List(testRecipe)
  
  private val mockRepository = new RecipeRepository {
    override def createTable: IO[SQLException, Unit] = ZIO.unit

    override def insert(recipe: Recipe): IO[SQLException, Int] =
      if (recipe.title == "fail") ZIO.fail(new SQLException("Insert failed"))
      else ZIO.succeed(1)

    override def get(id: Int): IO[SQLException, Option[Recipe]] =
      if (id == testRecipe.id) ZIO.succeed(Some(testRecipe))
      else ZIO.succeed(None)

    override def getAll: IO[SQLException, List[Recipe]] = ZIO.succeed(testRecipes)

    override def update(recipe: Recipe): IO[SQLException, Long] =
      if (recipe.id == testRecipe.id) ZIO.succeed(1L)
      else ZIO.succeed(0L)

    override def delete(id: Int): IO[SQLException, Long] =
      if (id == testRecipe.id) ZIO.succeed(1L)
      else ZIO.succeed(0L)
  }

  val mockRepositoryLayer: ULayer[RecipeRepository] = ZLayer.succeed(mockRepository)

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("RecipeServiceSpec")(
    test("createRecipe should successfully create a recipe") {
      val recipeForm = RecipeForm(
        title = "New Recipe",
        makingTime = 20,
        serves = 2,
        ingredients = "Ingredients",
        cost = 5
      )

      val testEffect = for {
        service <- ZIO.service[RecipeService]
        recipe  <- service.createRecipe(recipeForm)
      } yield assertTrue(
        recipe.id == 1,
        recipe.title == recipeForm.title,
        recipe.makingTime == recipeForm.makingTime,
        recipe.serves == recipeForm.serves,
        recipe.ingredients == recipeForm.ingredients,
        recipe.cost == recipeForm.cost
      )

      testEffect
    },
    test("createRecipe should fail when repository fails") {
      val recipeForm = RecipeForm(
        title = "fail",
        makingTime = 20,
        serves = 2,
        ingredients = "Ingredients",
        cost = 5
      )

      val testEffect = for {
        service <- ZIO.service[RecipeService]
        result  <- service.createRecipe(recipeForm).either
      } yield assert(result)(
        isLeft(equalTo(AppError.DatabaseError("Insert failed")))
      )

      testEffect
    },
    test("getRecipe should return a recipe when it exists") {
      val testEffect = for {
        service <- ZIO.service[RecipeService]
        recipe  <- service.getRecipe(1)
      } yield assertTrue(recipe == testRecipe)

      testEffect
    },
    test("getRecipe should fail with NotFound when recipe does not exist") {
      val testEffect = for {
        service <- ZIO.service[RecipeService]
        result  <- service.getRecipe(99).either
      } yield assert(result)(
        isLeft(equalTo(AppError.NotFound("No recipe found")))
      )

      testEffect
    },
    test("getAllRecipes should return all recipes") {
      val testEffect = for {
        service <- ZIO.service[RecipeService]
        recipes <- service.getAllRecipes
      } yield assertTrue(recipes == testRecipes)

      testEffect
    },
    test("updateRecipe should update recipe when it exists") {
      val recipeForm = RecipeForm(
        title = "Updated Recipe",
        makingTime = 25,
        serves = 3,
        ingredients = "Updated Ingredients",
        cost = 15
      )

      val testEffect = for {
        service       <- ZIO.service[RecipeService]
        updatedRecipe <- service.updateRecipe(1, recipeForm)
      } yield assertTrue(
        updatedRecipe.id == testRecipe.id,
        updatedRecipe.title == recipeForm.title,
        updatedRecipe.makingTime == recipeForm.makingTime,
        updatedRecipe.serves == recipeForm.serves,
        updatedRecipe.ingredients == recipeForm.ingredients,
        updatedRecipe.cost == recipeForm.cost
      )

      testEffect
    },
    test("updateRecipe should fail with NotFound when recipe does not exist") {
      val recipeForm = RecipeForm(
        title = "Updated Recipe",
        makingTime = 25,
        serves = 3,
        ingredients = "Updated Ingredients",
        cost = 15
      )

      val testEffect = for {
        service <- ZIO.service[RecipeService]
        result  <- service.updateRecipe(99, recipeForm).either
      } yield assert(result)(
        isLeft(equalTo(AppError.NotFound("No recipe found")))
      )

      testEffect
    },
    test("deleteRecipe should delete recipe when it exists") {
      val testEffect = for {
        service <- ZIO.service[RecipeService]
        _       <- service.deleteRecipe(1)
      } yield assertCompletes

      testEffect
    },
    test("deleteRecipe should fail with NotFound when recipe does not exist") {
      val testEffect = for {
        service <- ZIO.service[RecipeService]
        result  <- service.deleteRecipe(99).either
      } yield assert(result)(
        isLeft(equalTo(AppError.NotFound("No recipe found")))
      )

      testEffect
    }
  ).provideLayer(mockRepositoryLayer >>> RecipeServiceImpl.live)
}
