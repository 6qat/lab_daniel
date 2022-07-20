ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

conflictWarning := ConflictWarning.disable

scalacOptions := Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8"
  // "-Xsource:3"
  // "-language:postfixOps",
  // "-language:strictEquality"
)

val zioVersion = "2.0.0"
val zioConfigVersion = "3.0.1"

val akkaVersion = "2.6.19"
val akkaHttpVersion = "10.2.9"

val http4sVersion = "1.0.0-M34"
val circeVersion = "0.14.2"

val scalaTestVersion = "3.2.12"

val baseDependencies = Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.36"
)

val zioDependencies = Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-config" % zioConfigVersion,
  "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
  "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "io.circe" %% "circe-generic" % circeVersion
) //.map(_.cross(CrossVersion.for3Use2_13))

val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test"
//  "dev.zio" %% "zio-akka-cluster" % "0.2.0"
).map(_.cross(CrossVersion.for3Use2_13))

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

libraryDependencies ++= (
  baseDependencies ++ zioDependencies ++ akkaDependencies
).map(_.withSources().withJavadoc()) ++ testDependencies

lazy val root = (project in file("."))
  .settings(
    name := "lab_daniel",
    idePackagePrefix := Some("tc.lab.daniel")
  )
