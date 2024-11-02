import asyncio
import websockets

async def listen_to_scala_server():
	uri = "ws://127.0.0.1:8080/ws"

	async with websockets.connect(uri) as websocket:
		print("Connecté au serveur Scala, en attente des messages...")

		while True:
			try:
				await websocket.send("Hello")
				message = await websocket.recv()
				print(f"Message reçu du serveur : {message}")

				if message == "command:shutdown":
					break

			except websockets.ConnectionClosed:
				print("Connexion fermée par le serveur.")
				break
			except Exception as e:
				print(f"Erreur lors de la réception : {e}")
				break

asyncio.run(listen_to_scala_server())
