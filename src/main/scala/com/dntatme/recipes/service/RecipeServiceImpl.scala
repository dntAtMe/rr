package com.dntatme.recipes.service

import zio.*
import com.dntatme.recipes.model.*
import com.dntatme.recipes.repository.RecipeRepository
import com.dntatme.recipes.error.AppError

import java.sql.Timestamp
import java.time.Instant
import java.util.concurrent.TimeUnit

class RecipeServiceImpl(repository: RecipeRepository) extends RecipeService {

  override def createRecipe(recipeForm: RecipeForm): IO[AppError, Recipe] = {
    val currentTime = Timestamp(Instant.now().toEpochMilli)
    val recipe = Recipe(
      id = 0,
      title = recipeForm.title,
      makingTime = recipeForm.makingTime,
      serves = recipeForm.serves,
      ingredients = recipeForm.ingredients,
      cost = recipeForm.cost,
      createdAt = currentTime,
      updatedAt = currentTime
    )
    repository
      .insert(recipe)
      .map(id => recipe.copy(id = id))
      .mapError(e => AppError.DatabaseError(e.getMessage))
  }

  override def getRecipe(id: Int): IO[AppError, Recipe] = {
    repository
      .get(id)
      .mapError(e => AppError.DatabaseError(e.getMessage))
      .flatMap {
        case Some(recipe) => ZIO.succeed(recipe)
        case None => ZIO.fail(AppError.NotFound("No recipe found"))
      }
  }

  override def getAllRecipes: IO[AppError, List[Recipe]] = {
    repository.getAll.mapError(e => AppError.DatabaseError(e.getMessage))
  }

  override def updateRecipe(id: Int, recipeForm: RecipeForm): IO[AppError, Recipe] = {
    for {
      existingRecipe <- getRecipe(id)
      currentTime <- Clock.currentTime(TimeUnit.MILLISECONDS).map(new Timestamp(_))
      updatedRecipe = existingRecipe.copy(
        title = recipeForm.title,
        makingTime = recipeForm.makingTime,
        serves = recipeForm.serves,
        ingredients = recipeForm.ingredients,
        cost = recipeForm.cost,
        updatedAt = currentTime
      )
      _ <- repository.update(updatedRecipe).mapError(e => AppError.DatabaseError(e.getMessage))
    } yield updatedRecipe
  }

  override def deleteRecipe(id: Int): IO[AppError, Unit] = {
    repository.delete(id).mapError(e => AppError.DatabaseError(e.getMessage)).flatMap {
      case 0 => ZIO.fail(AppError.NotFound("No recipe found"))
      case _ => ZIO.unit
    }
  }
}

object RecipeServiceImpl {
  val live: ZLayer[RecipeRepository, Nothing, RecipeService] =
    ZLayer.fromFunction(new RecipeServiceImpl(_))
}
