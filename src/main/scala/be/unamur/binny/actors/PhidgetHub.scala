package be.unamur.binny.actors

import akka.actor.{Actor, Props}
import com.phidget22.{Hub, PhidgetException}

case object StartMonitoring
case class LidDistanceReading(distance: Double)
case class FootDistanceReading(distance: Double)
case class ForceReading(force: Double)
case class TouchReading(touched: Double)

class PhidgetHub(hub: Hub) extends Actor
{
	override def preStart(): Unit = {
		// Ouvrir la connexion avec le hub
		try
		{
			hub.open(5000)
			println("Hub connecté")
			self ! StartMonitoring // Une fois le hub connecté, on démarre la surveillance des capteurs
		}
		catch
		{
			case e: PhidgetException =>
				println(s"Erreur lors de la connexion au hub: ${e.getMessage}")
				self ! preStart()
			case _ => println("Erreur inconnue lors de la connexion au hub")
		}
	}

	override def postStop(): Unit = {
		hub.close() // Fermer la connexion au hub quand l'acteur est arrêté
		println("Hub déconnecté")
	}

	def receive: Receive = {
		case StartMonitoring =>
			println("Démarrage de la surveillance des capteurs")
			val lidDistanceSensorActor = context.actorOf(Props(new LidDistanceSensorActor(0)), "lidDistanceSensorActor")
			val forceSensorActor = context.actorOf(Props(new ForceSensorActor(1)), "forceSensorActor")
			val touchSensorActor = context.actorOf(Props(new TouchSensorActor(2)), "touchSensorActor")
		case other => println(s"Message inconnu: $other")
	}
}