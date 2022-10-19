val zioVersion = "2.0.2"
val zioJsonVersion = "0.3.0-RC10"
val logbackVersion = "1.2.11"
val zioConfigVersion = "3.0.1"
val zioMockVersion = "1.0.0-RC8"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        // name := "scala-effect-analyzer",
        organization := "io.github.bbarker.eff.analyzer",
        version := "0.0.1",
        scalaVersion := "3.1.3",
        semanticdbEnabled := true,
        semanticdbVersion := scalafixSemanticdb.revision,
        scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
      )
    ),
    name := "zio-quickstart",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-config" % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "dev.zio" %% "zio-json" % zioJsonVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-test-junit" % zioVersion % Test,
      "dev.zio" %% "zio-mock" % zioMockVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
      "org.ow2.asm" % "asm" % "9.4",
      "org.ow2.asm" % "asm-tree" % "9.4",
      "org.ow2.asm" % "asm-util" % "9.4"
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .enablePlugins(JavaAppPackaging)
