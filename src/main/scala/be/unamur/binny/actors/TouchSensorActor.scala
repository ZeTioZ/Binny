package be.unamur.binny.actors

import akka.actor.Actor
import com.phidget22.{PhidgetException, VoltageRatioInput, VoltageRatioInputSensorChangeEvent, VoltageRatioSensorType}

class TouchSensorActor(channel: Int) extends Actor
{
	private case class TouchReading(touched: Double)
	private val touchSensor = new VoltageRatioInput()
	private var touched: Double = 0.0

	override def preStart(): Unit = {
		println("Démarrage du capteur de toucher...")
		try
		{
			touchSensor.setIsHubPortDevice(true)
			touchSensor.setHubPort(channel)
			touchSensor.setDeviceSerialNumber(672221)
			touchSensor.open(5000)
			touchSensor.setSensorType(VoltageRatioSensorType.PN_1129)
			println("Capteur de toucher connecté")

			touchSensor.addSensorChangeListener((event: VoltageRatioInputSensorChangeEvent) => {
				if (event.getSensorValue != touched)
					touched = event.getSensorValue
					self ! TouchReading(touched)
			})
		}
		catch
		{
			case e: PhidgetException =>
				println(s"Erreur lors de la connexion au capteur de toucher: ${e.getMessage}")
				self ! preStart()
			case _ => println("Erreur inconnue lors de la connexion au capteur de toucher")
		}
	}

	override def receive: Receive = {
		case TouchReading(touched) =>
			val isTouched = touched > 0.5
			context.parent ! TouchedUpdate(isTouched)
			println(s"Touched: $isTouched")
	}

	override def postStop(): Unit = {
		println("Capteur de toucher déconnecté")
		touchSensor.close()
	}
}
