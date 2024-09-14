package com.dntatme.recipes.repository

import zio.*
import io.getquill.jdbczio.Quill

import javax.sql.DataSource

object TestDataSource {
  val layer: ZLayer[Any, Throwable, DataSource] =
    //    Quill.DataSource.fromConfig(
    //      ConfigFactory.load("application-test.conf").getConfig("db")
    //    )
    Quill.DataSource.fromPrefix("db")
}
