name := "ecollo"

import sbt._
import sbt.Keys._

val akkaHttpSpayJson    = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10"
val alpakka             = "com.lightbend.akka" %% "akka-stream-alpakka-file" % "1.1.2"
val testkit             = "com.typesafe.akka" %% "akka-http-testkit" % "10.1.11" % "test"

//val hdfs                = "org.apache.hadoop" % "hadoop-hdfs" % "3.0.0"
//val hadoopClient        = "org.apache.hadoop" % "hadoop-client" % "3.0.0"

val sparkstreaming      = "org.apache.spark" %% "spark-streaming" % "2.4.2"
val cassandraconn       = "com.datastax.spark" %% "spark-cassandra-connector" % "2.4.2"
val sparksql            = "org.apache.spark" %% "spark-sql" % "2.4.4"

val scalaTest           = "org.scalatest" %% "scalatest" % "3.0.8" % "test"

val log4j2Version       = "2.11.1"
val log4jApi            = "org.apache.logging.log4j" % "log4j-api" % log4j2Version
val log4jSlf4j          = "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4j2Version
val log4jCore           = "org.apache.logging.log4j" % "log4j-core" % log4j2Version
val disruptor           = "com.lmax" % "disruptor" % "3.4.2" // for async log4j2
val framelessDS         = "org.typelevel" %% "frameless-dataset" % "0.8.0"


lazy val commonSettings = Seq(
  scalaVersion := "2.12.10",
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-Xlog-reflective-calls",
    "-Xlint",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-deprecation",
    "-feature",
    "-language:_",
    "-unchecked"
  ),
  scalacOptions in(Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused-import"),
  scalacOptions in(Test, console) := (scalacOptions in(Compile, console)).value
)

def appModule(moduleID: String): Project = {
  Project(id = moduleID, base = file(moduleID))
    .settings(name := moduleID)
    .withId(moduleID)
    .settings(commonSettings)
}

lazy val root =
  Project(id = "ecollo", base = file("."))
    .settings(
      name := "ecollo",
      skip in publish := true,
      version := "0.1"
    )
    .withId("ecollo")
    .settings(commonSettings)
    .aggregate(
      ecolloLoadingPipeline,
      dataModel,
      dataIngress,
      dataEgress,
      dataMediator,
      dataPrinter
    )

lazy val ecolloLoadingPipeline = appModule("ecollo-loading-pipeline")
  .enablePlugins(CloudflowApplicationPlugin)
  .settings(commonSettings)
  .settings(
    name := "ecollo-loading-pipeline",
    runLocalConfigFile := Some("resources/local.conf")
  )
  .dependsOn(dataIngress, dataEgress, dataMediator, dataPrinter)

lazy val dataModel = appModule("data-model")
  .enablePlugins(CloudflowLibraryPlugin)
  .settings(
    commonSettings,
    (sourceGenerators in Compile) += (avroScalaGenerateSpecific in Compile).taskValue,
    (avroScalaSpecificCustomTypes in Compile) := {
      avrohugger.format.SpecificRecord.defaultTypes.copy(
        timestampMillis = avrohugger.types.JavaTimeInstant)
    }
  )

lazy val dataIngress = appModule("data-ingress")
  .enablePlugins(CloudflowAkkaStreamsLibraryPlugin)
  .settings(commonSettings,
    libraryDependencies ++= Seq(
      akkaHttpSpayJson,
      log4jApi,
      log4jSlf4j,
      log4jCore,
      disruptor,
      scalaTest,
      alpakka
    )
  )
  .dependsOn(dataModel)

lazy val dataEgress = appModule("data-egress")
  .enablePlugins(CloudflowSparkLibraryPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      sparkstreaming,
      cassandraconn,
      sparksql,
      log4jApi,
      log4jSlf4j,
      log4jCore,
      framelessDS
    )
  )
  .settings(
    parallelExecution in Test := false
  )
  .dependsOn(dataModel)

lazy val dataMediator = appModule("data-mediator")
  .enablePlugins(CloudflowFlinkLibraryPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      log4jApi,
      log4jSlf4j,
      log4jCore
    )
  )
  .settings(
    parallelExecution in Test := false
  )
  .dependsOn(dataModel)

lazy val dataPrinter = appModule("data-printer")
  .enablePlugins(CloudflowFlinkLibraryPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      log4jApi,
      log4jSlf4j,
      log4jCore
    )
  )
  .settings(
    parallelExecution in Test := false
  )
  .dependsOn(dataModel)