import org.typelevel.sbt.gha.JavaSpec.Distribution.Zulu

ThisBuild / organization       := "com.github.pjfanning"
ThisBuild / crossScalaVersions := List("2.12.17", "2.13.10", "3.3.0")
ThisBuild / scalaVersion       := "2.13.10"

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec(Zulu, "8"))
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

ThisBuild / mimaPreviousArtifacts := Set.empty
ThisBuild / resolvers += "Apache Nexus Snapshots".at("https://repository.apache.org/content/repositories/snapshots/")

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
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
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
  val awsSDKVersion    = "2.17.113"
  val pekkoVersion     = "0.0.0+26669-ec5b6764-SNAPSHOT"
  val pekkoHttpVersion = "0.0.0+4411-6fe04045-SNAPSHOT"

  Seq(
    "org.apache.pekko"      %% "pekko-stream"    % pekkoVersion,
    "org.apache.pekko"      %% "pekko-http"      % pekkoHttpVersion,
    "software.amazon.awssdk" % "http-client-spi" % awsSDKVersion,
    "software.amazon.awssdk" % "s3" % awsSDKVersion % "test" exclude ("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk" % "dynamodb" % awsSDKVersion % "test" exclude ("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk" % "sqs" % awsSDKVersion % "test" exclude ("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk" % "sns" % awsSDKVersion % "test" exclude ("software.amazon.awssdk", "netty-nio-client"),
    "software.amazon.awssdk" % "kinesis" % awsSDKVersion % "test" exclude ("software.amazon.awssdk", "netty-nio-client"),
    "com.dimafeng"      %% "testcontainers-scala" % "0.40.14"  % "test",
    "junit"              % "junit"                % "4.13.2"   % "test",
    "org.scalatest"     %% "scalatest"            % "3.2.15"   % "it,test",
    "org.scalatestplus" %% "junit-4-13"           % "3.2.15.0" % "it,test",
    "ch.qos.logback"     % "logback-classic"      % "1.2.12"   % "it,test"
  )
}
