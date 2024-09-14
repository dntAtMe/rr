package com.dntatme.recipes

import com.dntatme.recipes.api.RecipeApi
import com.dntatme.recipes.model.Recipe
import com.dntatme.recipes.repository.{PostgresRecipeRepository, RecipeRepository}
import com.dntatme.recipes.service.RecipeServiceImpl
import io.getquill.*
import zio.*
import zio.Console.printLine

import javax.sql.DataSource
import io.getquill.jdbczio.Quill
import zio.http.Server

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

object MainApp extends ZIOAppDefault:

  private val dataSourceLayer: ZLayer[Any, Throwable, DataSource] =
    Quill.DataSource.fromPrefix("db")

  val server =
    Server
      .serve(RecipeApi.routes)
      .provide(
        dataSourceLayer,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
        PostgresRecipeRepository.live,
        RecipeServiceImpl.live,
        Server.default
      )

  private def logError[R, E](error: E): URIO[R, Unit] =
    printLine(s"An error occurred: $error").orDie

  override def run: ZIO[Any, Throwable, ExitCode] =
    val program = for
      repository <- ZIO.service[RecipeRepository]
      _ <- repository.createTable
      currentTime <- Clock.currentTime(TimeUnit.MILLISECONDS).map(Timestamp(_))
      recipe = Recipe(
        id = 0,
        title = "Pancakes",
        makingTime = 30,
        serves = 4,
        ingredients = Seq("Flour", "Eggs", "Milk").mkString(", "),
        cost = 10,
        createdAt = currentTime,
        updatedAt = currentTime
      )
      id <- repository.insert(recipe)
      _ <- printLine(s"Inserted Recipe with ID: $id")
      retrievedRecipe <- repository.get(id)
      _ <- printLine(s"Retrieved Recipe: $retrievedRecipe")
      updatedRecipe = recipe.copy(id = id, title = "Blueberry Pancakes", updatedAt = currentTime)
      _ <- repository.update(updatedRecipe)
      _ <- printLine("Updated Recipe")
      retrievedUpdatedRecipe <- repository.get(id)
      _ <- printLine(s"Retrieved Updated Recipe: $retrievedUpdatedRecipe")
      _ <- repository.delete(id)
      _ <- printLine("Deleted Recipe")
      afterDeletion <- repository.get(id)
      _ <- printLine(s"After Deletion, Retrieved Recipe: $afterDeletion")
    yield ()

    server
      .tapError(logError(_))
      .exitCode
