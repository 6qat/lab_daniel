ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

val zioV = "2.0.0"
val zioConfigV = "3.0.1"

val baseDependencies = Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.36",
)

val zioDependencies = Seq(
  "dev.zio" %% "zio" % zioV,
  "dev.zio" %% "zio-config" % zioConfigV,
  "dev.zio" %% "zio-config-typesafe" % zioConfigV,
  "dev.zio" %% "zio-config-magnolia" % zioConfigV,
)

libraryDependencies ++= (
  baseDependencies ++ zioDependencies
  ).map(_.withSources().withJavadoc())

lazy val root = (project in file("."))
  .settings(
    name := "lab_daniel",
    idePackagePrefix := Some("tc.lab.daniel")
  )
