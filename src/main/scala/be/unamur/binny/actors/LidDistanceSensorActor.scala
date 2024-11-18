package be.unamur.binny.actors

import akka.actor.Actor
import com.phidget22.{DistanceSensor, DistanceSensorDistanceChangeEvent, PhidgetException}

class LidDistanceSensorActor(channel: Int) extends Actor
{
	private case class LidDistanceReading(distance: Double)
	private val lidDistanceSensor = new DistanceSensor()
	private var lastDistance: Double = 0.0

	override def preStart(): Unit = {
		println("Démarrage du capteur de distance...")
		try
		{
			lidDistanceSensor.setHubPort(channel)
			lidDistanceSensor.open(5000)
			println("Capteur de distance connecté")

			lidDistanceSensor.addDistanceChangeListener((event: DistanceSensorDistanceChangeEvent) => {
				val distance: Double = event.getDistance
				if (distance != lastDistance && Math.abs(distance - lastDistance) > 2) {
					lastDistance = distance
					self ! LidDistanceReading(distance)
				}
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
			context.parent ! DistanceUpdate(distance)
	}

	override def postStop(): Unit = {
		println("Capteur de distance déconnecté")
		lidDistanceSensor.close()
	}
}
