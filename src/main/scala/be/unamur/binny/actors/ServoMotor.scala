package be.unamur.binny.actors

import akka.actor.Actor
import be.unamur.binny.SharedState
import com.phidget22.{PhidgetException, RCServo}

import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}

private case object StartMotor
private case object EngageMotor
private case object DisengageMotor
private case class setAngle(angle: Double)
private case class ServoUpdate(angle: Double)

class ServoMotor(sharedState: SharedState, servo: RCServo) extends Actor
{
	private val scheduler = Executors.newScheduledThreadPool(1)
	private var scheduledDisengage: Option[ScheduledFuture[?]] = None
	private var scheduledEngage: Option[ScheduledFuture[?]] = None

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
			servo.setTargetPosition(0)
			scheduleEngage()
			self ! ServoUpdate(0)
			scheduleDisengage()

		case setAngle(angle) =>
			println(s"Réglage de l'angle du moteur à $angle")
			servo.setTargetPosition(angle)
			scheduleEngage()
			self ! ServoUpdate(angle)
			scheduleDisengage()

		case EngageMotor =>
			println("Engagement du moteur")
			servo.setEngaged(true)

		case DisengageMotor =>
			println("Désengagement du moteur")
			servo.setEngaged(false)

		case ServoUpdate(angle) => sharedState.servoAngle = angle

		case other => println(s"Message inconnu: $other")
	}

	private def scheduleEngage(): Unit =
	{
		for (scheduled <- scheduledEngage) yield scheduled.cancel(false)
		scheduledEngage = Some(scheduler.schedule(new Runnable {
			override def run(): Unit = self ! EngageMotor
		}, 1000, TimeUnit.MILLISECONDS))
	}

	private def scheduleDisengage(): Unit =
	{
		for (scheduled <- scheduledDisengage) yield scheduled.cancel(false)
		scheduledDisengage = Some(scheduler.schedule(new Runnable {
			override def run(): Unit = self ! DisengageMotor
		}, 2500, TimeUnit.MILLISECONDS))
	}
}