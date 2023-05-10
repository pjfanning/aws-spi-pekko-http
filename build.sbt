import org.typelevel.sbt.gha.JavaSpec.Distribution.Zulu

ThisBuild / organization := "com.github.pjfanning"
ThisBuild / crossScalaVersions := List("2.12.17", "2.13.10")
ThisBuild / scalaVersion := "2.13.10"

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec(Zulu, "8"))
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.Equals(Ref.Branch("main")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.CI_DEPLOY_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.CI_DEPLOY_USERNAME }}",
      "CI_SNAPSHOT_RELEASE" -> "+publishSigned"
    )
  )
)

ThisBuild / mimaPreviousArtifacts := Set.empty
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
  val pekkoVersion = "0.0.0+26656-898c6970-SNAPSHOT"
  val pekkoHttpVersion = "0.0.0+4345-fa1cb9cb-SNAPSHOT"

  Seq(
    "org.apache.pekko"        %% "pekko-stream"            % pekkoVersion,
    "org.apache.pekko"        %% "pekko-http"              % pekkoHttpVersion,
    "software.amazon.awssdk"  %  "http-client-spi"         % awsSDKVersion,
    "org.scala-lang.modules"  %% "scala-java8-compat"      % "1.0.2",
    "org.scala-lang.modules"  %% "scala-collection-compat" % "2.7.0",

    "software.amazon.awssdk"  %  "s3"                      % awsSDKVersion   % "test" exclude("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk"  %  "dynamodb"                % awsSDKVersion   % "test" exclude("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk"  %  "sqs"                     % awsSDKVersion   % "test" exclude("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk"  %  "sns"                     % awsSDKVersion   % "test" exclude("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk"  %  "kinesis"                 % awsSDKVersion   % "test" exclude("software.amazon.awssdk", "netty-nio-client"),

    "com.dimafeng"            %% "testcontainers-scala"    % "0.40.7"        % "test",

    "junit"                   %  "junit"                   % "4.13.2"        % "test",

    "org.scalatest"           %% "scalatest"               % "3.2.15"        % "it,test",
    "org.scalatestplus"       %% "junit-4-13"              % "3.2.15.0"      % "it,test",
    "ch.qos.logback"          %  "logback-classic"         % "1.2.11"        % "it,test"
  )
}
