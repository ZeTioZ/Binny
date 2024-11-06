import json
import requests


def send_request(message):
	url = "http://26.22.108.117:1234/v1/chat/completions"

	# Your request payload
	payload = {
		"model": "llama-3.2-3b-instruct-uncensored-i1",
		"messages": [
			{ "role": "system", "content": "You are a trash bin named \"Binny\" that always try to be funny and kid friendly. The user will give you images of trash and you will have to answer according to these rules: plastic, metal and drink carton into the blue bag, food scraps in green bag, miscellaneous into black bag and styrofoam, cardboard or glass in none of these bags. Always add at the end of the message a little reminder of which bag you reminded the user to use with the following format \"bag:bag_color\", replace bag_color with the color of the bag you've give to the user. It's extremely important to add that reminder in that format! Never send a message that respond to a trash color without that reminder in that format!" },
			{ "role": "user", "content": message }
		],
		"temperature": 0.7,
		"max_tokens": -1,
		"stream": False
	}

	headers = {
		"Content-Type": "application/json"
	}

	# Send POST request
	response = requests.post(url, headers=headers, data=json.dumps(payload))

	# Check for successful response
	if response.status_code == 200:
		return response.json()
	else:
		return {"error": "Request failed with status code " + str(response.status_code)}


def main():
	print("Welcome to the Chatbot! Type 'quit' to exit.")

	while True:
		user_input = input("You: ")
		if user_input.lower() == 'quit':
			break

		try:
			response = send_request(user_input)
			bot_response = response.get("choices")[0].get("message").get("content") if response.get("choices") else "Désolé, je n'ai pas pu récupérer une réponse."
		except requests.exceptions.ConnectionError:
			bot_response = "Désolé, je n'ai pas pu récupérer une réponse, veuillez vérifier votre connexion au réseau."
		print("Binny:", bot_response)

if __name__ == "__main__":
	main()