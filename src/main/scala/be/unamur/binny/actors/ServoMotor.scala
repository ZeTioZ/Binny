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
			servo.open(5000)
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
			servo.setEngaged(true)
			self ! ServoUpdate(0)
			scheduleDisengage()

		case setAngle(angle) =>
			// Sometimes, the servo seems to be disconnected, so we try to reconnect it
			servo.setTargetPosition(angle)
			servo.setEngaged(true)
			self ! ServoUpdate(angle)
			scheduleDisengage()

		case DisengageMotor =>
			println("Désengagement du moteur")
			servo.setEngaged(false)

		case ServoUpdate(angle) => sharedState.servoAngle = angle

		case other => println(s"Message inconnu: $other")
	}

	private def scheduleDisengage(): Unit =
	{
		for (scheduled <- scheduledDisengage) yield scheduled.cancel(false)
		scheduledDisengage = Some(scheduler.schedule(new Runnable {
			override def run(): Unit = self ! DisengageMotor
		}, 1500, TimeUnit.MILLISECONDS))
	}
}