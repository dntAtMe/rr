package com.dntatme.recipes.repository

import com.dntatme.recipes.model.Recipe
import zio.IO

import java.sql.SQLException

trait RecipeRepository:
  def createTable: IO[SQLException, Unit]
  def insert(recipe: Recipe): IO[SQLException, Int]
  def get(id: Int): IO[SQLException, Option[Recipe]]
  def getAll: IO[SQLException, List[Recipe]]
  def update(recipe: Recipe): IO[SQLException, Long]
  def delete(id: Int): IO[SQLException, Long]
