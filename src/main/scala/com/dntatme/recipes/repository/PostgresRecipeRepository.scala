package com.dntatme.recipes.repository

import com.dntatme.recipes.model.Recipe
import io.getquill._
import io.getquill.context.ExecutionInfo
import io.getquill.jdbczio.Quill
import zio._

import java.sql.{SQLException, Timestamp}
import javax.sql.DataSource

class PostgresRecipeRepository(quill: Quill.Postgres[SnakeCase], dataSource: DataSource)
    extends RecipeRepository {

  import quill._

  implicit val ec: ExecutionInfo = ExecutionInfo.unknown

  def createTable: IO[SQLException, Unit] = {
    val createTableQuery = quote {
      sql"""
        CREATE TABLE IF NOT EXISTS recipe (
          id SERIAL PRIMARY KEY,
          title VARCHAR(255) NOT NULL,
          making_time INTEGER NOT NULL,
          serves INTEGER NOT NULL,
          ingredients TEXT NOT NULL,
          cost INTEGER NOT NULL,
          created_at TIMESTAMP NOT NULL,
          updated_at TIMESTAMP NOT NULL
        )
      """.as[Action[Unit]]
    }
    run(createTableQuery).unit.provideEnvironment(ZEnvironment(dataSource))
  }

  def insert(recipe: Recipe): IO[SQLException, Int] =
    run(query[Recipe].insertValue(lift(recipe)).returningGenerated(_.id))
      .provideEnvironment(ZEnvironment(dataSource))

  def get(id: Int): IO[SQLException, Option[Recipe]] =
    run(query[Recipe].filter(_.id == lift(id)))
      .map(_.headOption)
      .provideEnvironment(ZEnvironment(dataSource))

  def getAll: IO[SQLException, List[Recipe]] =
    run(query[Recipe])
      .provideEnvironment(ZEnvironment(dataSource))

  def update(recipe: Recipe): IO[SQLException, Long] =
    run(query[Recipe].filter(_.id == lift(recipe.id)).updateValue(lift(recipe)))
      .provideEnvironment(ZEnvironment(dataSource))

  def delete(id: Int): IO[SQLException, Long] =
    run(query[Recipe].filter(_.id == lift(id)).delete)
      .provideEnvironment(ZEnvironment(dataSource))
}

object PostgresRecipeRepository {
  val live: ZLayer[Quill.Postgres[SnakeCase] with DataSource, Nothing, RecipeRepository] =
    ZLayer.fromZIO {
      for {
        quill <- ZIO.service[Quill.Postgres[SnakeCase]]
        dataSource <- ZIO.service[DataSource]
      } yield new PostgresRecipeRepository(quill, dataSource)
    }
}
