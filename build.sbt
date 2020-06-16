organization in ThisBuild := "uk.gov.hmrc"
version in ThisBuild := "0.1.0"
scalaVersion in ThisBuild := "2.12.11"

val twirl = (project in file("twirl")).
  settings ( 
    name := "hmrc-uniform-twirl",
    libraryDependencies ++= Seq(
      "com.luketebbs.uniform"      %% "interpreter-play26"      % "4.10.0",
      "com.beachape"               %% "enumeratum"              % "1.6.0",
      "com.typesafe.play"          %% "play"                    % "2.6.25" % "provided",

      // test dependencies
      "org.scalatest"              %% "scalatest"               % "3.1.1"  % Test,
      "net.ruippeixotog"           %% "scala-scraper"           % "2.2.0"  % Test,
      "org.typelevel"              %% "cats-testkit-scalatest"  % "1.0.1"  % Test,
      "net.sourceforge.htmlcleaner" % "htmlcleaner"             % "2.24"   % Test
    ),
    scalacOptions -= "-Xfatal-warnings"
  ).enablePlugins(SbtTwirl)

val scalatags = (project in file("scalatags")).
  settings ( 
    name := "hmrc-uniform-scalatags",
    libraryDependencies ++= Seq(
      "com.luketebbs.uniform"      %% "interpreter-play26"      % "4.10.0",
      "com.beachape"               %% "enumeratum"              % "1.6.0",
      "com.lihaoyi"                %% "scalatags"               % "0.8.2",

      // test dependencies
      "org.scalatest"              %% "scalatest"               % "3.1.1"  % Test,
      "net.ruippeixotog"           %% "scala-scraper"           % "2.2.0"  % Test,
      "org.typelevel"              %% "cats-testkit-scalatest"  % "1.0.1"  % Test,
      "net.sourceforge.htmlcleaner" % "htmlcleaner"             % "2.24"   % Test
    ),
    scalacOptions -= "-Xfatal-warnings"
  ).enablePlugins(SbtTwirl)


lazy val `example-project` = (project in file("example-project"))
  .dependsOn(twirl)
  .settings(
    scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      filters,
      guice
    )
  )
  .enablePlugins(play.sbt.PlayScala)
