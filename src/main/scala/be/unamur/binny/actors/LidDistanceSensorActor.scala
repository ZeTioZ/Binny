package be.unamur.binny.actors

import akka.actor.Actor
import com.phidget22.{DistanceSensor, DistanceSensorDistanceChangeEvent, PhidgetException}

class LidDistanceSensorActor(channel: Int) extends Actor
{
	private val lidDistanceSensor = new DistanceSensor()

	override def preStart(): Unit = {
		println("Démarrage du capteur de distance de capot...")
		try
		{
			lidDistanceSensor.setHubPort(channel)
			lidDistanceSensor.open(5000)
			println("Capteur de distance de capot connecté")

			lidDistanceSensor.addDistanceChangeListener((event: DistanceSensorDistanceChangeEvent) => {
				val distance: Double = event.getDistance
				self ! LidDistanceReading(distance)
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
		case LidDistanceReading(distance) =>
			println(s"Distance: $distance")
	}

	override def postStop(): Unit = {
		println("Capteur de distance de capot déconnecté")
		lidDistanceSensor.close()
	}
}
