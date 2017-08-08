lazy val root = project.in(file("."))
  .settings(name := "agni")
  .settings(allSettings)
  .settings(noPublishSettings)
  .aggregate(core, `twitter-util`, monix, fs2)
  .dependsOn(core, `twitter-util`, monix, fs2)

lazy val allSettings = Seq.concat(
  buildSettings,
  baseSettings,
  publishSettings,
  scalariformSettings
)

lazy val buildSettings = Seq(
  organization := "com.github.yanana",
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11", "2.12.3"),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")
)

val datastaxVersion = "3.3.0"
val catsVersion = "0.9.0"
val shapelessVersion = "2.3.2"
val scalacheckVersion = "1.13.5"
val scalatestVersion = "3.0.3"
val catbirdVersion = "0.15.0"
val monixVersion = "2.3.0"
val fs2Version = "0.9.7"

lazy val coreDeps = Seq(
  "com.datastax.cassandra" % "cassandra-driver-core" % datastaxVersion,
  "org.typelevel" %% "cats" % catsVersion,
  "com.chuusai" %% "shapeless" % shapelessVersion
)

lazy val testDeps = Seq(
  "org.scalacheck" %% "scalacheck" % scalacheckVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion
) map (_ % "test")

lazy val baseSettings = Seq(
  scalacOptions ++= compilerOptions,
  scalacOptions in (Compile, console) := compilerOptions,
  scalacOptions in (Compile, test) := compilerOptions,
  libraryDependencies ++= (coreDeps ++ testDeps).map(_.withSources),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  scalacOptions in (Compile, console) ~= (_ filterNot (_ == "-Ywarn-unused-import"))
)

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/yanana/agni")),
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/yanana/agni"),
      "scm:git:git@github.com:yanana/agni.git"
    )
  ),
  pomExtra :=
    <developers>
      <developer>
        <id>yanana</id>
        <name>Shun Yanaura</name>
        <url>https://github.com/yanana</url>
      </developer>
      <developer>
        <id>tkrs</id>
        <name>Takeru Sato</name>
        <url>https://github.com/tkrs</url>
      </developer>
    </developers>
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val core = project.in(file("core"))
  .settings(allSettings)
  .settings(
    description := "agni core",
    moduleName := "agni-core",
    name := "core"
  )

lazy val `twitter-util` = project.in(file("twitter-util"))
  .settings(allSettings)
  .settings(
    description := "agni twitter-util",
    moduleName := "agni-twitter-util",
    name := "twitter-util"
  )
  .settings(
    libraryDependencies ++= Seq(
      "io.catbird" %% "catbird-util" % catbirdVersion
    )
  )
  .dependsOn(core)

lazy val monix = project.in(file("monix"))
  .settings(allSettings)
  .settings(
    description := "agni monix",
    moduleName := "agni-monix",
    name := "monix"
  )
  .settings(
    libraryDependencies ++= Seq(
      "io.monix" %% "monix-eval" % monixVersion,
      "io.monix" %% "monix-cats" % monixVersion
    )
  )
  .dependsOn(core)

lazy val fs2 = project.in(file("fs2"))
  .settings(allSettings)
  .settings(
    description := "agni fs2",
    moduleName := "agni-fs2",
    name := "fs2"
  )
  .settings(
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-cats" % "0.3.0"
    )
  )
  .dependsOn(core)

lazy val benchmarks = project.in(file("benchmarks"))
  .settings(noPublishSettings)
  .settings(
    description := "agni benchmarks",
    moduleName := "agni-benchmarks",
    name := "benchmarks",
    scalaVersion := "2.12.3",
    crossScalaVersions := Seq("2.12.3"),
    libraryDependencies ++= coreDeps ++ Seq(
      "io.catbird" %% "catbird-util" % catbirdVersion,
      "io.monix" %% "monix-eval" % monixVersion,
      "io.monix" %% "monix-cats" % monixVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-cats" % "0.3.0",
      "com.github.ben-manes.caffeine" % "caffeine" % "2.4.0",
      "com.github.ben-manes.caffeine" % "guava" % "2.4.0"
    )
  )
  .enablePlugins(JmhPlugin)
  .dependsOn(`twitter-util`, monix, fs2)

lazy val examples = project.in(file("examples"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    description := "agni examples",
    moduleName := "agni-examples",
    name := "examples",
    scalaVersion := "2.12.3",
    crossScalaVersions := Seq("2.12.3"),
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % "1.7.13",
      "org.scalatest" %% "scalatest" % scalatestVersion
    )
  )
  .dependsOn(`twitter-util`)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Xlint"
)
