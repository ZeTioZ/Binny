name := "Binny"

version := "1.0.0"

scalaVersion := "3.5.1"

organization := "be.unamur"

libraryDependencies ++= {
	Seq(
		"com.typesafe.akka" %% "akka-actor-typed" % "2.9.6",
		"com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.9.6" % Test,
		"org.slf4j" % "slf4j-simple" % "2.0.16",
		"org.slf4j" % "slf4j-api" % "2.0.16",
	)
}

Compile / run / mainClass := Some("be.unamur.binny.HelloWorld")
Compile / packageBin / mainClass := Some("be.unamur.binny.HelloWorld")
resolvers ++= Seq("Akka library repository" at "https://repo.akka.io/maven")
scalacOptions ++= Seq("-unchecked", "-deprecation")