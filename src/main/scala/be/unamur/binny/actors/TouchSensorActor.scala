package be.unamur.binny.actors

import akka.actor.Actor
import com.phidget22.{PhidgetException, VoltageRatioInput, VoltageRatioInputSensorChangeEvent, VoltageRatioSensorType}

class TouchSensorActor(channel: Int) extends Actor
{
	val touchSensor = new VoltageRatioInput()

	override def preStart(): Unit = {
		println("Démarrage du capteur de touché...")
		try
		{
			touchSensor.setIsHubPortDevice(true)
			touchSensor.setHubPort(channel)
			touchSensor.setDeviceSerialNumber(672221)
			touchSensor.open(5000)
			touchSensor.setSensorType(VoltageRatioSensorType.PN_1129)
			println("Capteur de touché connecté")

			touchSensor.addSensorChangeListener((event: VoltageRatioInputSensorChangeEvent) => {
				val touched: Double = event.getSensorValue
				self ! TouchReading(touched)
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
		case TouchReading(touched) =>
			val isTouched = touched > 0.5
			println(s"Touched: $isTouched")
	}

	override def postStop(): Unit = {
		println("Capteur de force déconnecté")
		touchSensor.close()
	}
}
