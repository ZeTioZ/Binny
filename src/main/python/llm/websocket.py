import websockets

async def send_ws_message(uri: str, message: str):
	uri = f"ws://{uri}/ws"

	try:
		async with websockets.connect(uri) as websocket:
			print("Connecté au serveur Scala, envoi du message...")
			print(f"Message envoyé au serveur : {message}")

			try:
				await websocket.send(message)
				response = await websocket.recv()
				print(f"Message reçu du serveur : {response}")
			except websockets.ConnectionClosed:
				print("Connexion fermée par le serveur.")
			except Exception as e:
				print(f"Erreur lors de l'envoi ou de la réception d'un message: {e}")
	except Exception as e:
		print(f"Erreur lors de la connexion au serveur: {e}")
