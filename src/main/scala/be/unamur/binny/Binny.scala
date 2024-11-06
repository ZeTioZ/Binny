package be.unamur.binny

import akka.actor.{ActorRef, ActorSystem, Props}
import be.unamur.binny.actors.PhidgetHub
import be.unamur.binny.animation.VirtualAssistantEyes
import be.unamur.binny.websocket.WebSocket
import com.phidget22.Hub
import scalafx.application.JFXApp3


object Binny extends JFXApp3
{
	private val system: ActorSystem = ActorSystem("IoTSystem")
	private val hub: Hub = new Hub()
	val phidgetHub: ActorRef = system.actorOf(Props(new PhidgetHub(hub)), "phidgetHub")
	val webSocket: Unit = new WebSocket().start()

	override def start(): Unit = {
		new VirtualAssistantEyes().start()
	}
}