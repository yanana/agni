import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

val preferences =
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(DanglingCloseParenthesis, Force)

lazy val root = project.in(file("."))
  .settings(name := "agni")
  .settings(allSettings)
  .settings(noPublishSettings)
  .aggregate(core, `twitter-util`, monix, `cats-effect`, free)
  .dependsOn(core, `twitter-util`, monix, `cats-effect`, free)

lazy val allSettings = Seq.concat(
  buildSettings,
  baseSettings,
  publishSettings,
  Seq(preferences)
)

lazy val buildSettings = Seq(
  organization := "com.github.yanana",
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11", "2.12.3"),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.5")
)

val datastaxVersion = "3.3.0"
val catsVersion = "1.0.1"
val iotaVersion = "0.3.4"
val shapelessVersion = "2.3.2"
val scalacheckVersion = "1.13.5"
val scalatestVersion = "3.0.4"
val catbirdVersion = "17.12.0"
val monixVersion = "3.0.0-M3"
val mockitoVersion = "2.11.0"
val catsEffectVersion = "0.8"
val caffeineVersion = "2.6.0"

lazy val coreDeps = Seq(
  "com.datastax.cassandra" % "cassandra-driver-core" % datastaxVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "com.chuusai" %% "shapeless" % shapelessVersion
)

lazy val testDeps = Seq(
  "org.scalacheck" %% "scalacheck" % scalacheckVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion,
  "org.mockito" % "mockito-core" % mockitoVersion
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
  publish := ((): Unit),
  publishLocal := ((): Unit),
  publishArtifact := false
)

lazy val core = project.in(file("core"))
  .settings(allSettings)
  .settings(
    sourceGenerators in Compile += (sourceManaged in Compile).map(Boilerplate.gen).taskValue
  )
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
    name := "twitter-util",
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
    name := "monix",
    libraryDependencies ++= Seq(
      "io.monix" %% "monix-eval" % monixVersion,
      "io.monix" %% "monix-tail" % monixVersion
    )
  )
  .dependsOn(core)

lazy val `cats-effect` = project.in(file("cats-effect"))
  .settings(allSettings)
  .settings(
    description := "agni cats-effect",
    moduleName := "agni-cats-effect",
    name := "cats-effect",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion
    )
  )
  .dependsOn(core)

lazy val free = project.in(file("free"))
  .settings(allSettings)
  .settings(
    description := "agni free",
    moduleName := "agni-free",
    name := "free",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-free" % catsVersion,
      "io.frees" %% "iota-core"  % iotaVersion
    )
  )
  .dependsOn(core)

lazy val benchmarks = project.in(file("benchmarks"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    description := "agni benchmarks",
    moduleName := "agni-benchmarks",
    name := "benchmarks",
    crossScalaVersions := Seq("2.12.3"),
    libraryDependencies ++= coreDeps ++ Seq(
      "io.catbird" %% "catbird-util" % catbirdVersion,
      "io.monix" %% "monix-eval" % monixVersion,
      "io.monix" %% "monix-tail" % monixVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "com.github.ben-manes.caffeine" % "caffeine" % caffeineVersion,
      "com.github.ben-manes.caffeine" % "guava" % caffeineVersion
    ),
    scalacOptions ++= Seq(
      "-opt:l:inline",
      "-opt-inline-from:**",
      "-opt-warnings"
    )
  )
  .enablePlugins(JmhPlugin)
  .dependsOn(`twitter-util`, monix, `cats-effect`, free)

lazy val examples = project.in(file("examples"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    description := "agni examples",
    moduleName := "agni-examples",
    name := "examples",
    crossScalaVersions := Seq("2.12.3"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-free" % catsVersion,
      "org.typelevel" %% "cats-effect" % "0.5",
      "org.slf4j" % "slf4j-simple" % "1.7.13",
      "org.scalatest" %% "scalatest" % scalatestVersion
    )
  )
  .dependsOn(`twitter-util`, free)

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
