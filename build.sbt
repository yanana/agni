lazy val root = project.in(file("."))
  .settings(allSettings)
  .settings(noPublishSettings)
  .aggregate(core, examples)
  .dependsOn(core, examples)

lazy val allSettings = buildSettings ++ baseSettings ++ publishSettings ++ scalariformSettings

lazy val buildSettings = Seq(
  organization := "com.github.yanana",
  scalaVersion := "2.11.7"
)

val datastaxVersion = "2.1.9"
val catsVersion = "0.3.0"
val shapelessVersion = "2.2.5"
val scalacheckVersion = "1.12.5"

lazy val coreDeps = Seq(
  "com.datastax.cassandra" % "cassandra-driver-core" % datastaxVersion classifier "shaded" excludeAll(
    ExclusionRule(organization = "io.netty")
  ),
  "org.spire-math" %% "cats" % catsVersion,
  "com.chuusai" %% "shapeless" % shapelessVersion,
  "org.scodec" %% "scodec-bits" % "1.0.12"
)

lazy val testDeps = Seq(
  "org.scalacheck" %% "scalacheck" % scalacheckVersion
) map (_ % "test")

lazy val baseSettings = Seq(
  scalacOptions ++= compilerOptions,
  scalacOptions in (Compile, console) := compilerOptions,
  scalacOptions in (Compile, test) := compilerOptions,
  libraryDependencies ++= coreDeps ++ testDeps,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")
)

lazy val publishSettings = Seq(
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
  .settings(
    description := "agni core",
    moduleName := "agni-core",
    name := "core"
  )
  .settings(allSettings: _*)

lazy val examples = project.in(file("examples"))
  .settings(
    description := "agni examples",
    moduleName := "agni-examples",
    name := "examples"
  )
  .settings(allSettings: _*)
  .settings(noPublishSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % "1.7.13",
      "io.netty" % "netty-all" % "5.0.0.Alpha2"
    )
  )
  .dependsOn(core)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Yinline-warnings",
  "-Xlint"
)

