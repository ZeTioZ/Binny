name := "Binny"

version := "1.0.0"

scalaVersion := "3.5.1"

organization := "be.unamur"

val AkkaVersion = "2.10.0"
libraryDependencies ++= {
	Seq(
		"com" % "phidget" % "22",
		"com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
		"com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
		"org.slf4j" % "slf4j-simple" % "2.0.16",
		"com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
		"com.typesafe.akka" %% "akka-http" % "10.7.0",
		"com.typesafe.akka" %% "akka-stream" % AkkaVersion,
	)
}

Compile / run / bgRun / packageBin / mainClass := Some("be.unamur.binny.Binny")
resolvers ++= Seq(
				  "Akka library repository" at "https://repo.akka.io/maven",
				  "ZeTioZ Repository" at "https://nexus.donatog.live/repository/maven-releases/"
			  )
scalacOptions ++= Seq("-unchecked", "-deprecation")