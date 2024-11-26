package be.unamur.binny.actors

import akka.actor.Actor
import be.unamur.binny.SharedState
import com.phidget22.{PhidgetException, RCServo}

import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}

private case object StartMotor
private case object DisengageMotor
private case class setAngle(angle: Double)
private case class ServoUpdate(angle: Double)

class ServoMotor(sharedState: SharedState, servo: RCServo) extends Actor
{
	private val scheduler = Executors.newScheduledThreadPool(1)
	private var scheduledDisengage: Option[ScheduledFuture[?]] = None

	override def preStart(): Unit =
	{
		try
		{
			servo.setDeviceSerialNumber(307756)
			while(!servo.getIsOpen)
			{
				println("Ouverture du moteur...")
				servo.open(5000)
			}
			println("Servo moteur connecté")
			self ! StartMotor
		}
		catch
		{
			case e: PhidgetException =>
				println(s"Erreur lors de la connexion au moteur: ${e.getMessage}")
				self ! preStart()
			case _ => println("Erreur inconnue lors de la connexion au moteur")
		}
	}

	override def postStop(): Unit =
	{
		servo.close()
		println("Servo Moteur déconnecté")
		scheduler.shutdown()
	}

	def receive: Receive =
	{
		case StartMotor =>
			println("Démarrage du moteur")
			try
			{
				self ! setAngle(0)
			}
			catch
			{
				case e: PhidgetException =>
					println(s"Erreur lors du démarrage du moteur: ${e.getMessage}")
				case ex =>
					println(s"Erreur inconnue lors du désengagement du moteur ${ex.getMessage}")
			}
		case setAngle(angle) =>
			try
			{
				println(s"Réglage de l'angle du moteur à $angle")
				servo.setTargetPosition(angle)
				servo.setEngaged(true)
				self ! ServoUpdate(angle)
				scheduleDisengage()
			}
			catch
			{
				case e: PhidgetException =>
					println(s"Erreur lors du réglage de l'angle du moteur: ${e.getMessage}")
				case ex =>
					println(s"Erreur inconnue lors du désengagement du moteur ${ex.getMessage}")
			}
		case DisengageMotor =>
			try
			{
				println("Désengagement du moteur")
				servo.setEngaged(false)
			}
			catch
			{
				case e: PhidgetException =>
					println(s"Erreur lors du désengagement du moteur: ${e.getMessage}")
				case ex =>
					println(s"Erreur inconnue lors du désengagement du moteur ${ex.getMessage}")
			}

		case ServoUpdate(angle) => sharedState.servoAngle = angle

		case other => println(s"Message inconnu: $other")
	}

	private def scheduleDisengage(): Unit =
	synchronized {
		for (scheduled <- scheduledDisengage) yield scheduled.cancel(false)
		scheduledDisengage = Some(scheduler.schedule(new Runnable {
			override def run(): Unit = self ! DisengageMotor
		}, 500, TimeUnit.MILLISECONDS))
	}
}