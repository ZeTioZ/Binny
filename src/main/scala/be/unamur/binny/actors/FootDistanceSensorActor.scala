package be.unamur.binny.actors

import akka.actor.Actor
import com.phidget22.{PhidgetException, VoltageRatioInput, VoltageRatioInputSensorChangeEvent, VoltageRatioSensorType}

class FootDistanceSensorActor(channel: Int) extends Actor
{
	private case class IRReading(touched: Double)
	private val irReading = new VoltageRatioInput()
	private var distance: Double = 0.0

	override def preStart(): Unit =
	{
		println("Démarrage du capteur de touché...")
		try
		{
			irReading.setIsHubPortDevice(true)
			irReading.setHubPort(channel)
			irReading.setDeviceSerialNumber(672221)
			irReading.open(5000)
			irReading.setSensorType(VoltageRatioSensorType.PN_1103)
			println("Capteur infrarouge connecté")

			irReading.addSensorChangeListener((event: VoltageRatioInputSensorChangeEvent) =>
			{
				if (event.getSensorValue != distance)
					distance = event.getSensorValue
					self ! IRReading(distance)
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
		case IRReading(distance) =>
			val isNear = distance > 0.5
			println(s"Is Near: $isNear")
			context.parent ! NearUpdate(isNear)

	}

	override def postStop(): Unit =
	{
		println("Capteur infrarouge déconnecté")
		irReading.close()
	}
}
