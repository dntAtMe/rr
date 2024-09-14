package com.dntatme.recipes.service

import zio._
import com.dntatme.recipes.model._
import com.dntatme.recipes.error.AppError

trait RecipeService {
  def createRecipe(recipeForm: RecipeForm): IO[AppError, Recipe]
  def getRecipe(id: Int): IO[AppError, Recipe]
  def getAllRecipes: IO[AppError, List[Recipe]]
  def updateRecipe(id: Int, recipeForm: RecipeForm): IO[AppError, Recipe]
  def deleteRecipe(id: Int): IO[AppError, Unit]
}
