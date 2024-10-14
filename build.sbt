name := "Binny"

version := "1.0.0"

scalaVersion := "3.5.1"

organization := "be.unamur"

libraryDependencies ++= {
	Seq(
		"com" % "phidget" % "22",
		"com.typesafe.akka" %% "akka-actor-typed" % "2.9.6",
		"com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.9.6" % Test,
		"org.slf4j" % "slf4j-simple" % "2.0.16",
		"org.slf4j" % "slf4j-api" % "2.0.16",
	)
}

Compile / run / bgRun / packageBin / mainClass := Some("scala.be.unamur.binny.Binny")
resolvers ++= Seq(
				  "Akka library repository" at "https://repo.akka.io/maven",
				  "ZeTioZ Repository" at "https://nexus.donatog.live/repository/maven-releases/"
			  )
scalacOptions ++= Seq("-unchecked", "-deprecation")