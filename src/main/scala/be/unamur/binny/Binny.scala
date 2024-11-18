package be.unamur.binny

import akka.actor.{ActorRef, ActorSystem, Props}
import be.unamur.binny.actors.PhidgetHub
import be.unamur.binny.actors.ServoMotor
import be.unamur.binny.animation.VirtualAssistantEyes
import be.unamur.binny.websocket.WebSocket
import com.phidget22.{Hub, RCServo}
import scalafx.application.JFXApp3

object Binny extends JFXApp3
{
	private val sharedState: SharedState = new SharedState()
	private val system: ActorSystem = ActorSystem("IoTSystem")
	private val hub: Hub = new Hub()
	private val servo: RCServo = new RCServo()
	private val servoMotor: ActorRef = system.actorOf(Props(new ServoMotor(sharedState, servo)), "servoMotor")
	private val phidgetHub: ActorRef = system.actorOf(Props(new PhidgetHub(sharedState, hub, servoMotor)), "phidgetHub")
	val webSocket: Unit = new WebSocket().start()

	override def start(): Unit = {
		new VirtualAssistantEyes().start()
	}
}