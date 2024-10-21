package be.unamur.binny.websockets

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.Flow

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path}
import akka.http.scaladsl.server.Route

class WebSocket extends Thread
{
	implicit val system: ActorSystem = ActorSystem("binny-websocket")
	implicit val executionContext: ExecutionContextExecutor  = system.dispatcher

	// Fonction pour gérer les messages WebSocket
	private def messageHandler: Flow[Message, Message, Any] =
		Flow[Message].map {
			case textMessage: TextMessage.Strict =>
				textMessage.text match {
					case "Hello" =>
						TextMessage.Strict("Hello, World!")
					case _ =>
						TextMessage.Strict("Message inconnu")
				}
			case _: Message =>
				TextMessage.Strict("Message non supporté")
		}

	// Définir la route WebSocket
	private val route: Route =
		path("ws") {
			handleWebSocketMessages(messageHandler)
		}

	override def run(): Unit = {
		// Démarrer le serveur HTTP
		val bindingFuture: Future[ServerBinding] = Http().newServerAt("localhost", 8080).bind(route)

		println("Serveur WebSocket démarré sur ws://localhost:8080/ws")
		println("Appuyez sur ENTER pour arrêter le serveur...")

		StdIn.readLine() // Attendre une entrée pour arrêter le serveur

		// Arrêter le serveur
		bindingFuture
			.flatMap(_.unbind())
			.onComplete(_ => system.terminate())
	}
}
