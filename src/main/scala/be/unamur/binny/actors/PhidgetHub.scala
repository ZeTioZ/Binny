package be.unamur.binny.actors

import akka.actor.{Actor, ActorRef, Props}
import be.unamur.binny.SharedState
import com.phidget22.{Hub, PhidgetException}

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

private case object StartMonitoring
private case class DistanceUpdate(distance: Double)
private case class TouchedUpdate(touched: Boolean)
private case class LidFreeUpdate(lid: Boolean)
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
		case LidFreeUpdate(lid) => sharedState.isLidFree = lid
		case NearUpdate(near) =>
			sharedState.isNear = near
			toggleLidOnNear()
		case "open:blue" =>
			openColoredLid("blue")
		case "open:black" =>
			openColoredLid("black")
		case "open:green" =>
			openColoredLid("green")
		case other => println(s"Message inconnu: $other")
	}

	private def toggleLidOnNear(): Unit =
	{
		try
		{
			if (sharedState.isNear)
			{
				if (sharedState.isLidFree && sharedState.servoAngle == 90)
				{
					servoMotor.tell(setAngle(0), self)
					sharedState.servoAngle = 0
				}
				else if (sharedState.isLidFree && sharedState.servoAngle == 0)
				{
					servoMotor.tell(setAngle(90), self)
					sharedState.servoAngle = 90
				}
				else if (!sharedState.isLidFree)
				{
					notifyLidBlocked()
				}
			}
		}
		catch
		{
			case e: PhidgetException => println(s"Erreur lors de la modification de l'état de la sortie: ${e.getMessage}")
			case ex => println(s"Erreur inconnue lors de la modification de l'état de la sortie ${ex.getMessage}")
		}
	}
	private def toggleLidOnTouch(): Unit =
	{
		try
		{
			if (sharedState.isTouched)
			{
				if (sharedState.isLidFree && sharedState.servoAngle == 90)
				{
					servoMotor.tell(setAngle(0), self)
					sharedState.servoAngle = 0
				}
				else if (sharedState.isLidFree && sharedState.servoAngle == 0)
				{
					servoMotor.tell(setAngle(90), self)
					sharedState.servoAngle = 90
				}
				else if (!sharedState.isLidFree)
				{
					notifyLidBlocked()
				}
			}
		}
		catch
		{
			case e: PhidgetException => println(s"Erreur lors de la modification de l'état de la sortie: ${e.getMessage}")
			case ex => println(s"Erreur inconnue lors de la modification de l'état de la sortie ${ex.getMessage}")
		}
	}

	private def openColoredLid(color: String): Unit =
	{
		try
		{
			color match
			{
				case "blue" =>
					if (sharedState.isLidFree && sharedState.servoAngle == 0)
					{
						println("Ouverture de la poubelle bleue")
						servoMotor.tell(setAngle(90), self)
						sharedState.servoAngle = 90
					}
					else if (!sharedState.isLidFree)
					{
						notifyLidBlocked()
					}
				case "black" =>
					if (sharedState.isLidFree && sharedState.servoAngle == 0)
					{
						println("Ouverture de la poubelle noire")
						servoMotor.tell(setAngle(90), self)
						sharedState.servoAngle = 90
					}
					else if (!sharedState.isLidFree)
					{
						notifyLidBlocked()
					}
				case "green" =>
					if (sharedState.isLidFree && sharedState.servoAngle == 0)
					{
						println("Ouverture de la poubelle verte")
						servoMotor.tell(setAngle(90), self)
						sharedState.servoAngle = 90
					}
					else if (!sharedState.isLidFree)
					{
						notifyLidBlocked()
					}
				case _ => println("Couleur inconnue")
			}
		}
		catch
		{
			case e: PhidgetException => println(s"Erreur lors de l'ouverture de la poubelle: ${e.getMessage}")
			case ex => println(s"Erreur inconnue lors de la modification de l'état de la sortie ${ex.getMessage}")
		}
	}

	private def notifyLidBlocked(): Unit =
	{
		val synthesizerUrl = "http://127.0.0.1:8124/synthesize/"
		val message = "The%20lid%20seems%20to%20be%20blocked%2C%20free%20it%20up%20before%20I%20can%20open%20it."
		val client = HttpClient.newHttpClient()
		val request = HttpRequest.newBuilder()
			.uri(URI.create(s"$synthesizerUrl$message"))
			.GET()
			.build()

		try {
			val response = client.send(request, HttpResponse.BodyHandlers.ofString())
			println("Notification sent to the speech synthesis engine.")
		} catch {
			case e: Exception => println(s"Error sending notification: ${e.getMessage}")
		}
	}
}