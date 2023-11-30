ThisBuild / organization       := "com.github.pjfanning"
ThisBuild / crossScalaVersions := List("2.12.18", "2.13.12", "3.3.1")
ThisBuild / scalaVersion       := "2.13.12"

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.Equals(Ref.Branch("main")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE"      -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"          -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD"   -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME"   -> "${{ secrets.SONATYPE_USERNAME }}",
      "CI_SNAPSHOT_RELEASE" -> "+publishSigned"
    )
  )
)

ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("it:compile", "test"), name = Some("Build project")))

val isScala3 = Def.setting(scalaBinaryVersion.value == "3")

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-target:jvm-1.8", "-encoding", "UTF-8", "-unchecked", "-deprecation") ++ (
    if (!isScala3.value)
      Seq("-opt:l:inline", "-opt-inline-from:<sources>")
    else
      Seq.empty // Inliner not available for Scala 3 yet
  ),
  description := "An alternative non-blocking async http engine for aws-sdk-java-v2 based on pekko-http",
  name        := "aws-spi-pekko-http",
  licenses += ("Apache-2.0", new URL("https://github.com/pjfanning/aws-spi-pekko-http/blob/main/LICENSE")),
  startYear            := Some(2023),
  homepage             := Some(url("https://github.com/pjfanning/aws-spi-pekko-http")),
  organizationHomepage := Some(url("https://github.com/pjfanning/aws-spi-pekko-http")),
  developers := Developer("matsluni", "Matthias Lüneberg", "", url("https://github.com/matsluni")) ::
    Developer("pjfanning", "PJ Fanning", "", url("https://github.com/pjfanning")) :: Nil,
  scmInfo := Some(
    ScmInfo(
      browseUrl = url("https://github.com/pjfanning/aws-spi-pekko-http.git"),
      connection = "scm:git:git@github.com:pjfanning/aws-spi-pekko-http.git"
    )
  )
)

lazy val IntegrationTest = config("it") extend Test

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= deps,
    Test / fork            := true,
    publishMavenStyle      := true,
    Test / publishArtifact := false,
    pomIncludeRepository   := (_ => false),
    publishTo := Some(
      if (version.value.trim.endsWith("SNAPSHOT"))
        "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
      else
        "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    )
  )

lazy val deps = {
  val awsSDKVersion    = "2.20.162"
  val pekkoVersion     = "1.0.1"
  val pekkoHttpVersion = "1.0.0"

  Seq(
    "org.apache.pekko"      %% "pekko-stream"    % pekkoVersion,
    "org.apache.pekko"      %% "pekko-http"      % pekkoHttpVersion,
    "software.amazon.awssdk" % "http-client-spi" % awsSDKVersion,
    "software.amazon.awssdk" % "s3" % awsSDKVersion % "test" exclude ("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk" % "dynamodb" % awsSDKVersion % "test" exclude ("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk" % "sqs" % awsSDKVersion % "test" exclude ("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk" % "sns" % awsSDKVersion % "test" exclude ("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk" % "kinesis" % awsSDKVersion % "test" exclude ("software.amazon.awssdk", "netty-nio-client"),
    "com.dimafeng"      %% "testcontainers-scala" % "0.41.0"   % "test",
    "junit"              % "junit"                % "4.13.2"   % "test",
    "org.scalatest"     %% "scalatest"            % "3.2.17"   % "it,test",
    "org.scalatestplus" %% "junit-4-13"           % "3.2.17.0" % "it,test",
    "ch.qos.logback"     % "logback-classic"      % "1.2.12"   % "it,test"
  )
}
