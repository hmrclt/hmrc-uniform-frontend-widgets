libraryDependencies ++= Seq(
  "com.luketebbs.uniform"   %% "interpreter-play26" % "4.10.0",
  "com.beachape"            %% "enumeratum"         % "1.6.0",
  "com.typesafe.play"       %% "twirl-api"          % "1.3.15",

  // test dependencies
  "org.scalatest"           %% "scalatest"          % "3.1.1" % Test,
  "net.ruippeixotog"        %% "scala-scraper"      % "2.2.0" % Test,
  "org.typelevel"           %% "cats-testkit-scalatest" % "1.0.1" % Test,
  "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.24" % Test
)

enablePlugins(SbtTwirl)

scalacOptions -= "-Xfatal-warnings"
