package be.unamur.binny

import akka.actor.{ActorSystem, Props}
import be.unamur.binny.actors.PhidgetHub
import be.unamur.binny.animation.VirtualAssistantEyes
import be.unamur.binny.websocket.WebSocket
import com.phidget22.Hub
import scalafx.application.JFXApp3


object Binny extends App
{
	private val system: ActorSystem = ActorSystem("IoTSystem")
	private val hub = new Hub()
	val phidgetHub = system.actorOf(Props(new PhidgetHub(hub)), "phidgetHub")
	val webSocket: Unit = new WebSocket().start()
	JFXAppLauncher.main(Array())
}

object JFXAppLauncher extends JFXApp3 {
	override def start(): Unit = {
		new be.unamur.binny.animation.VirtualAssistantEyes().start()
	}
}