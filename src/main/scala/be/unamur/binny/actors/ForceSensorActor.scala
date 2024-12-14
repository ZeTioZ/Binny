package be.unamur.binny.actors

import akka.actor.Actor
import com.phidget22.{PhidgetException, VoltageRatioInput, VoltageRatioInputSensorChangeEvent, VoltageRatioSensorType}

class ForceSensorActor(channel: Int) extends Actor
{
	private case class ForceReading(force: Double)
	private val forceSensor = new VoltageRatioInput()
	private var force: Double = 0.0

	override def preStart(): Unit =
	{
		println("Démarrage du capteur de force...")
		try
		{
			forceSensor.setIsHubPortDevice(true)
			forceSensor.setHubPort(channel)
			forceSensor.setDeviceSerialNumber(672221)
			forceSensor.open(5000)
			forceSensor.setSensorType(VoltageRatioSensorType.PN_1106)
			println("Capteur de force connecté")

			forceSensor.addSensorChangeListener((event: VoltageRatioInputSensorChangeEvent) =>
			{
				if (event.getSensorValue != force && Math.abs(event.getSensorValue - force) > 0.01)
				{
					force = event.getSensorValue
					self ! ForceReading(force)
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

	override def receive: Receive =
	{
		case ForceReading(force) =>
			println(s"Lecture du capteur de force: $force")
			context.parent ! LidFreeUpdate(force < 0.1)
	}

	override def postStop(): Unit =
	{
		println("Capteur de force déconnecté")
		forceSensor.close()
	}
}
