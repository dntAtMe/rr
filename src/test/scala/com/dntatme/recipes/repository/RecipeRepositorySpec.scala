package com.dntatme.recipes.repository

import zio.*
import zio.test.Assertion.*
import io.getquill.*
import io.getquill.jdbczio.Quill

import javax.sql.DataSource
import com.dntatme.recipes.model.Recipe

import java.sql.Timestamp
import zio.test.*

import java.util.concurrent.TimeUnit

object RecipeRepositorySpec extends ZIOSpecDefault {
  override def spec: Spec[Any, Throwable] = suite("RecipeRepositorySpec")(
    test("createTable should create the recipe table") {
      for {
        repository <- ZIO.service[RecipeRepository]
        _          <- repository.createTable
      } yield assertCompletes
    },
    test("insert should add a new recipe and return its ID") {
      for {
        repository <- ZIO.service[RecipeRepository]
        _          <- repository.createTable
        timestamp <- Clock.currentTime(TimeUnit.MILLISECONDS)
        currentTime = new Timestamp(timestamp)
        recipe = Recipe(
          id = 0,
          title = "Test Recipe",
          makingTime = 15,
          serves = 2,
          ingredients = "Test Ingredients",
          cost = 5,
          createdAt = currentTime,
          updatedAt = currentTime
        )
        id <- repository.insert(recipe)
      } yield assert(id)(isGreaterThan(0))
    },
    test("get should retrieve a recipe by ID") {
      for {
        repository <- ZIO.service[RecipeRepository]
        _          <- repository.createTable
        timestamp <- Clock.currentTime(TimeUnit.MILLISECONDS)
        currentTime = new Timestamp(timestamp)
        recipe = Recipe(
          id = 0,
          title = "Test Recipe",
          makingTime = 15,
          serves = 2,
          ingredients = "Test Ingredients",
          cost = 5,
          createdAt = currentTime,
          updatedAt = currentTime
        )
        id          <- repository.insert(recipe)
        retrieved   <- repository.get(id)
      } yield assert(retrieved)(isSome(equalTo(recipe.copy(id = id))))
    },
    test("update should modify an existing recipe") {
      for {
        repository <- ZIO.service[RecipeRepository]
        _          <- repository.createTable
        timestamp <- Clock.currentTime(TimeUnit.MILLISECONDS)
        currentTime = new Timestamp(timestamp)
        recipe = Recipe(
          id = 0,
          title = "Test Recipe",
          makingTime = 15,
          serves = 2,
          ingredients = "Test Ingredients",
          cost = 5,
          createdAt = currentTime,
          updatedAt = currentTime
        )
        id          <- repository.insert(recipe)
        timestamp <- Clock.currentTime(TimeUnit.MILLISECONDS)
        currentTime = new Timestamp(timestamp)
        updatedRecipe = recipe.copy(
          id = id,
          title = "Updated Recipe",
          updatedAt = currentTime
        )
        _           <- repository.update(updatedRecipe)
        retrieved   <- repository.get(id)
      } yield assert(retrieved)(isSome(equalTo(updatedRecipe)))
    },
    test("delete should remove a recipe by ID") {
      for {
        repository <- ZIO.service[RecipeRepository]
        _          <- repository.createTable
        timestamp <- Clock.currentTime(TimeUnit.MILLISECONDS)
        currentTime = new Timestamp(timestamp)
        recipe = Recipe(
          id = 0,
          title = "Test Recipe",
          makingTime = 15,
          serves = 2,
          ingredients = "Test Ingredients",
          cost = 5,
          createdAt = currentTime,
          updatedAt = currentTime
        )
        id        <- repository.insert(recipe)
        _         <- repository.delete(id)
        retrieved <- repository.get(id)
      } yield assert(retrieved)(isNone)
    }
  ).provideShared(
    TestDataSource.layer,
    Quill.Postgres.fromNamingStrategy(SnakeCase),
    PostgresRecipeRepository.live
  )
}
