package be.unamur.binny

import akka.actor.{ActorSystem, Props}
import be.unamur.binny.actors.PhidgetHub
import com.phidget22.Hub


object Binny extends App
{
	private val system: ActorSystem = ActorSystem("IoTSystem")
	private val hub = new Hub()
	val phidgetHub = system.actorOf(Props(new PhidgetHub(hub)), "phidgetHub")
}