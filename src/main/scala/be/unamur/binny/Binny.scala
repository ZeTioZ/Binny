package be.unamur.binny

import akka.actor.{ActorSystem, Props}
import be.unamur.binny.actors.{InitHub, PhidgetHub}
import com.phidget22.Hub


object Binny extends App
{
	private val system: ActorSystem = ActorSystem("IoTSystem")
	private val hub = new Hub()
	val phidgetHub = system.actorOf(Props(new PhidgetHub(hub)), "phidgetHub")

	// Envoyer un message à l'acteur pour initialiser le hub
	phidgetHub ! InitHub(hub)
}