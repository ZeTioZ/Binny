package be.unamur.binny

import akka.actor.{ActorRef, ActorSystem, Props}
import be.unamur.binny.actors.PhidgetHub
import be.unamur.binny.animation.VirtualAssistantEyes
import be.unamur.binny.websocket.WebSocket
import com.phidget22.Hub
import scalafx.application.JFXApp3

import java.awt.Robot
import java.util.{Timer, TimerTask}


object Binny extends JFXApp3
{
	private val system: ActorSystem = ActorSystem("IoTSystem")
	private val hub: Hub = new Hub()
	val phidgetHub: ActorRef = system.actorOf(Props(new PhidgetHub(hub)), "phidgetHub")
	val webSocket: Unit = new WebSocket().start()

	private val robot = new Robot()
	private val timer = new Timer(true)

	timer.scheduleAtFixedRate(new TimerTask {
		override def run(): Unit = {
			val mouseX = java.awt.MouseInfo.getPointerInfo.getLocation.x
			val mouseY = java.awt.MouseInfo.getPointerInfo.getLocation.y
			robot.mouseMove(mouseX + 1, mouseY)
			robot.mouseMove(mouseX, mouseY)
		}
	}, 0, 60000)

	override def start(): Unit = {
		new VirtualAssistantEyes().start()
	}
}