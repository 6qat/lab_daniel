ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

scalacOptions := Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8",
  "-Xsource:3"
  // "-language:postfixOps",
  // "-language:strictEquality"
)

val zioV = "2.0.0"
val zioConfigV = "3.0.1"
val zioHttpV = "2.0.0-RC9" //"1.0.0.0-RC27" //"2.0.0-RC9"]]

val akkaV = "2.6.19"
val akkaHttpV = "10.2.9"

val baseDependencies = Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.36"
)

val zioDependencies = Seq(
  "dev.zio" %% "zio" % zioV,
  "dev.zio" %% "zio-streams" % zioV,
  "dev.zio" %% "zio-config" % zioConfigV,
  "dev.zio" %% "zio-config-typesafe" % zioConfigV,
  "dev.zio" %% "zio-config-magnolia" % zioConfigV
//  "io.d11" %% "zhttp" % zioHttpV

)

val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-http" % akkaHttpV
).map(_.cross(CrossVersion.for3Use2_13))

libraryDependencies ++= (
  baseDependencies ++ zioDependencies ++ akkaDependencies
).map(_.withSources().withJavadoc())

lazy val root = (project in file("."))
  .settings(
    name := "lab_daniel",
    idePackagePrefix := Some("tc.lab.daniel")
  )
