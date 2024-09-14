package com.dntatme.recipes.error

sealed trait AppError extends Throwable {
  def message: String
}

object AppError {
  final case class NotFound(message: String) extends AppError
  final case class InvalidInput(message: String) extends AppError
  final case class DatabaseError(message: String) extends AppError
  final case class UnexpectedError(message: String) extends AppError
}
