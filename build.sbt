import org.typelevel.sbt.gha.JavaSpec.Distribution.Temurin

ThisBuild / organization := "com.github.pjfanning"
ThisBuild / crossScalaVersions := List("2.12.15", "2.13.8")
ThisBuild / scalaVersion := crossScalaVersions.value.last

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec(Temurin, "8"))
ThisBuild / githubWorkflowPublishTargetBranches := Nil

ThisBuild / resolvers += "Apache Nexus Snapshots".at("https://repository.apache.org/content/repositories/snapshots/")

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-encoding", "UTF-8",
    "-unchecked",
    "-deprecation"),
  description := "An alternative non-blocking async http engine for aws-sdk-java-v2 based on pekko-http",
  name := "aws-spi-pekko-http",
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  startYear := Some(2023),
  homepage := Some(url("https://github.com/pjfanning/aws-spi-pekko-http")),
  organizationHomepage := Some(url("https://github.com/pjfanning/aws-spi-pekko-http")),
  developers := Developer("matsluni", "Matthias Lüneberg", "", url("https://github.com/matsluni")) ::
    Developer("pjfanning", "PJ Fanning", "", url("https://github.com/pjfanning")) :: Nil,
  scmInfo := Some(ScmInfo(
    browseUrl = url("https://github.com/pjfanning/aws-spi-pekko-http.git"),
    connection = "scm:git:git@github.com:pjfanning/aws-spi-pekko-http.git"
  ))
)

lazy val IntegrationTest = config("it") extend Test

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= deps,
    Test / fork := true,
    publishMavenStyle := true,
    Test / publishArtifact := false,
    pomIncludeRepository := (_ => false),
    publishTo := Some(
      if (version.value.trim.endsWith("SNAPSHOT"))
        "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
      else
        "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  )


lazy val deps = {
  val awsSDKVersion = "2.11.4"
  val pekkoVersion = "0.0.0+26610-defddc6a-SNAPSHOT"
  val pekkoHttpVersion = "0.0.0+4311-07201517-SNAPSHOT"

  Seq(
    "org.apache.pekko"        %% "pekko-stream"            % pekkoVersion,
    "org.apache.pekko"        %% "pekko-http"              % pekkoHttpVersion,
    "software.amazon.awssdk"  %  "http-client-spi"         % awsSDKVersion,
    "org.scala-lang.modules"  %% "scala-collection-compat" % "2.7.0",

    "software.amazon.awssdk"  %  "s3"                      % awsSDKVersion   % "test" exclude("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk"  %  "dynamodb"                % awsSDKVersion   % "test" exclude("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk"  %  "sqs"                     % awsSDKVersion   % "test" exclude("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk"  %  "sns"                     % awsSDKVersion   % "test" exclude("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk"  %  "kinesis"                 % awsSDKVersion   % "test" exclude("software.amazon.awssdk", "netty-nio-client"),

    "com.dimafeng"            %% "testcontainers-scala"    % "0.40.7"        % "test",

    "junit"                   %  "junit"                   % "4.13.2"        % "test",

    "org.scala-lang.modules"  %% "scala-java8-compat"      % "1.0.2"         % "it,test",
    "org.scalatest"           %% "scalatest"               % "3.2.15"        % "it,test",
    "org.scalatestplus"       %% "junit-4-13"              % "3.2.15.0"      % "it,test",
    "ch.qos.logback"          %  "logback-classic"         % "1.2.11"        % "it,test"
  )
}
