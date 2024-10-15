package be.unamur.binny.actors

import akka.actor.Actor
import com.phidget22.{DistanceSensor, DistanceSensorDistanceChangeEvent, PhidgetException}

class FootDistanceSensorActor(channel: Int) extends Actor
{
	private val footDistanceSensor = new DistanceSensor()

	override def preStart(): Unit = {
		println("Démarrage du capteur de distance de pieds...")
		try
		{
			footDistanceSensor.setHubPort(channel)
			footDistanceSensor.open(5000)
			println("Capteur de distance de pieds connecté")

			footDistanceSensor.addDistanceChangeListener((event: DistanceSensorDistanceChangeEvent) => {
				val distance: Double = event.getDistance
				self ! FootDistanceReading(distance)
			})
		}
		catch
		{
			case e: PhidgetException =>
				println(s"Erreur lors de la connexion au capteur de distance: ${e.getMessage}")
				self ! preStart()
			case _ => println("Erreur inconnue lors de la connexion au capteur de distance")
		}
	}

	override def receive: Receive = {
		case FootDistanceReading(distance) =>
			println(s"Distance: $distance")
	}

	override def postStop(): Unit = {
		println("Capteur de distance de pieds déconnecté")
		footDistanceSensor.close()
	}
}
