name := """toposoid-knowledge-register-web"""
organization := "com.ideal.linked"

version := "0.4-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(AutomateHeaderPlugin)

organizationName := "Linked Ideal LLC.[https://linked-ideal.com/]"
startYear := Some(2021)
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))


scalaVersion := "2.12.12"

libraryDependencies += guice
libraryDependencies += "com.ideal.linked" %% "scala-common" % "0.4-SNAPSHOT"
libraryDependencies += "com.ideal.linked" %% "toposoid-sentence-transformer-neo4j" % "0.4-SNAPSHOT"
libraryDependencies += "com.ideal.linked" %% "toposoid-feature-vectorizer" % "0.4-SNAPSHOT"
libraryDependencies += "io.jvm.uuid" %% "scala-uuid" % "0.3.1"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
