package be.unamur.binny.actors

import akka.actor.{Actor, ActorRef, Props}
import be.unamur.binny.SharedState
import com.phidget22.{Hub, PhidgetException}

private case object StartMonitoring
private case class DistanceUpdate(distance: Double)
private case class TouchedUpdate(touched: Boolean)
private case class LidOpenUpdate(lid: Boolean)
private case class NearUpdate(near: Boolean)

class PhidgetHub(sharedState: SharedState, hub: Hub, servoMotor: ActorRef) extends Actor
{
	override def preStart(): Unit =
	{
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

	override def postStop(): Unit =
	{
		hub.close() // Fermer la connexion au hub quand l'acteur est arrêté
		println("Hub déconnecté")
	}

	def receive: Receive =
	{
		case StartMonitoring =>
			println("Démarrage de la surveillance des capteurs")
			val footDistanceSensorActor = context.actorOf(Props(new FootDistanceSensorActor(0)), "footDistanceSensorActor")
			val forceSensorActor = context.actorOf(Props(new ForceSensorActor(1)), "forceSensorActor")
			val touchSensorActor = context.actorOf(Props(new TouchSensorActor(2)), "touchSensorActor")
			val lidDistanceSensorActor = context.actorOf(Props(new LidDistanceSensorActor(3)), "lidDistanceSensorActor")
		case DistanceUpdate(distance) => sharedState.lidDistance = distance
		case TouchedUpdate(touched) =>
			sharedState.isTouched = touched
			toggleLidOnTouch()
		case LidOpenUpdate(lid) => sharedState.isLidOpen = lid
		case NearUpdate(near) =>
			sharedState.isNear = near
			toggleLidOnNear()
		case other => println(s"Message inconnu: $other")
	}

	private def toggleLidOnNear(): Unit =
	{
		try
		{
			if (sharedState.isNear)
			{
				if (sharedState.isLidOpen)
				{
					servoMotor.tell(setAngle(0), self)
					sharedState.servoAngle = 0
				}
				else
				{
					servoMotor.tell(setAngle(90), self)
					sharedState.servoAngle = 90
				}
			}
		}
		catch
		{
			case e: PhidgetException => println(s"Erreur lors de la modification de l'état de la sortie: ${e.getMessage}")
			case _ => println("Erreur inconnue lors de la modification de l'état de la sortie")
		}
	}
	private def toggleLidOnTouch(): Unit =
	{
		try
		{
			if (sharedState.isTouched)
			{
				if (sharedState.isLidOpen)
				{
					servoMotor.tell(setAngle(0), self)
					sharedState.servoAngle = 0
				}
				else
				{
					servoMotor.tell(setAngle(90), self)
					sharedState.servoAngle = 90
				}
			}
		}
		catch
		{
			case e: PhidgetException => println(s"Erreur lors de la modification de l'état de la sortie: ${e.getMessage}")
			case _ => println("Erreur inconnue lors de la modification de l'état de la sortie")
		}
	}
}