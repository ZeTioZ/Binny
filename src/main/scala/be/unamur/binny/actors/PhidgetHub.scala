package be.unamur.binny.actors

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import be.unamur.binny.SharedState
import be.unamur.binny.animation.VirtualAssistantEyes
import com.phidget22.{Hub, PhidgetException}
import scala.concurrent.duration.DurationInt

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

private case object StartMonitoring
private case class DistanceUpdate(distance: Double)
private case class TouchedUpdate(touched: Boolean)
private case class LidFreeUpdate(lid: Boolean)
private case class NearUpdate(near: Boolean)

class PhidgetHub(sharedState: SharedState, hub: Hub, servoMotor: ActorRef, virtualAssistantEyes: VirtualAssistantEyes) extends Actor
{
	private var lidCloseScheduler: Option[Cancellable] = None

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
			case _ =>
				println("Erreur inconnue lors de la connexion au hub")
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
		case DistanceUpdate(distance) =>
			sharedState.lidDistance = distance
			if (distance < 300 && sharedState.servoAngle == 0)
			{
				scheduleLidClose()
			}
		case TouchedUpdate(touched) =>
			sharedState.isTouched = touched
			toggleLidManual()
		case LidFreeUpdate(lid) =>
			sharedState.isLidFree = lid
		case NearUpdate(near) =>
			sharedState.isNear = near
			toggleLidManual()
		case "lidClose" =>
			if (sharedState.servoAngle == 0)
			{
				toggleLid()
			}
		case "open:blue" =>
			openColoredLid("blue")
		case "open:black" =>
			openColoredLid("black")
		case "open:green" =>
			openColoredLid("green")
		case other =>
			println(s"Message inconnu: $other")
	}

	private def toggleLidManual(): Unit =
	{
		try
		{
			if (sharedState.isNear || sharedState.isTouched)
			{
				toggleLid()
			}
		}
		catch
		{
			case e: PhidgetException => println(s"Erreur lors de la modification de l'état de la sortie: ${e.getMessage}")
			case ex => println(s"Erreur inconnue lors de la modification de l'état de la sortie ${ex.getMessage}")
		}
	}

	private def toggleLid(): Unit =
	{
		if (sharedState.isLidFree && sharedState.servoAngle == 90)
		{
			servoMotor.tell(setAngle(0), self)
			sharedState.servoAngle = 0
			scheduleLidClose()
		}
		else if (sharedState.isLidFree && sharedState.servoAngle == 0)
		{
			servoMotor.tell(setAngle(90), self)
			virtualAssistantEyes.setBackgroundColor("default")
			sharedState.servoAngle = 90
			cancelLidCloseScheduler()
		}
		else if (!sharedState.isLidFree)
		{
			notifyLidBlocked()
			if (sharedState.servoAngle == 0)
			{
				scheduleLidClose()
			}
		}
	}

	private def openColoredLid(color: String): Unit =
	{
		try
		{
			if(!Seq("blue", "black", "green").contains(color)) return
			if (sharedState.isLidFree && sharedState.servoAngle == 90)
			{
				color match
				{
					case "blue" =>
						println("Ouverture de la poubelle bleue")
						virtualAssistantEyes.setBackgroundColor("blue")
					case "black" =>
						println("Ouverture de la poubelle noire")
						virtualAssistantEyes.setBackgroundColor("black")
					case "green" =>
						println("Ouverture de la poubelle verte")
						virtualAssistantEyes.setBackgroundColor("green")
					case _ =>
						println("Couleur inconnue")
						return
				}
				servoMotor.tell(setAngle(0), self)
				sharedState.servoAngle = 0
				scheduleLidClose()
			}
			else if (!sharedState.isLidFree)
			{
				notifyLidBlocked()
				if (sharedState.servoAngle == 0)
				{
					scheduleLidClose()
				}
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

		try
		{
			val response = client.send(request, HttpResponse.BodyHandlers.ofString())
			println("Notification sent to the speech synthesis engine.")
		}
		catch
		{
			case e: Exception =>
				println(s"Error sending notification: ${e.getMessage}")
		}
	}

	private def scheduleLidClose(): Unit =
	{
		import context.dispatcher
		cancelLidCloseScheduler()
		lidCloseScheduler = Some(context.system.scheduler.scheduleOnce(5.seconds, self, "lidClose"))
	}

	private def cancelLidCloseScheduler(): Unit =
	{
		lidCloseScheduler.foreach(_.cancel())
	}
}