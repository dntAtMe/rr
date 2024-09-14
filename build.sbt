ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

inThisBuild(
  List(
    scalaVersion := "3.3.3",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "Zio REST API"
  )

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.1.6",
  "dev.zio" %% "zio-json" % "0.6.2",
  "dev.zio" %% "zio-http" % "3.0.0-RC8",
  "dev.zio" %% "zio-test" % "2.1.4" % Test,
  "dev.zio" %% "zio-http-testkit" % "3.0.0-RC8" % Test,
  "dev.zio" %% "zio-test-sbt" % "2.1.6" % Test,
  "org.postgresql" % "postgresql" % "42.7.3",
  "dev.zio" %% "zio-jdbc" % "0.1.2",
  "io.getquill" %% "quill-jdbc-zio" % "4.8.4",
  "io.getquill" %% "quill-jdbc" % "4.8.4",
  "io.circe" %% "circe-core" % "0.14.7",
  "io.circe" %% "circe-generic" % "0.14.7",
  "io.circe" %% "circe-parser" % "0.14.9",
  "com.h2database" % "h2" % "2.2.224" % Test,
  "org.xerial" % "sqlite-jdbc" % "3.46.0.0" % Test


)