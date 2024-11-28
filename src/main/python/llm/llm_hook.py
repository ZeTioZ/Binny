import asyncio
import json
import requests
import speech_recognition as sr
import re

import websocket

llm_url = "http://127.0.0.1:1234/v1/chat/completions"
synthesizer_url = "http://127.0.0.1:8124/synthesize/"


def send_request(message):
	payload = {
		"model": "llama-3.2-3b-instruct-uncensored-i1",
		"messages": [
			{ "role": "system", "content": "You are a trash bin named \"Binny\" that always try to be funny and kid friendly. Don't correct the user if he mispronounce your name. The user can ask where to place trash you will have to answer according to these rules: plastic, metal and drink carton into the blue bag, food scraps in green bag, miscellaneous into black bag and styrofoam, cardboard or glass in none of these bags. Always answer with the phonetics so that a TTS machine can pronounce every word correctly. Always add at the end of the message a little reminder of which bag you reminded the user to use with the following format \"bag:bag_color\", replace bag_color with the color of the bag you've give to the user. It's extremely important to add that reminder in that format! Never send a message that respond to a trash color without that reminder in that format!" },
			{ "role": "user", "content": message }
		],
		"temperature": 0.7,
		"max_tokens": -1,
		"stream": False
	}

	headers = {
		"Content-Type": "application/json"
	}
	response = requests.post(llm_url, headers=headers, data=json.dumps(payload))
	if response.status_code == 200:
		return response.json()
	else:
		return {"error": "Request failed with status code " + str(response.status_code)}


def stt_to_tts():
	recognizer = sr.Recognizer()
	try:
		with sr.Microphone() as source2:
			recognizer.adjust_for_ambient_noise(source2, duration=0.5)
			audio2 = recognizer.listen(source2)
			user_input = recognizer.recognize_google(audio2)
			print(user_input)

			try:
				response = send_request(user_input)
				bot_response = response.get("choices")[0].get("message").get("content") if response.get("choices") else "Sorry, I did not understand what you said."
			except requests.exceptions.ConnectionError as e:
				bot_response = "Sorry, there is a network related issue: {0}".format(e)
			# Send the response to the synthesizer.
			sanitize = bot_response.replace("\n", " ").replace("\r", "").replace("\t", "").replace("\\", "")
			pattern = re.compile(r"(bag: *(?:blue|green|black|none))", re.IGNORECASE)
			colors = re.findall(pattern, sanitize)
			print(f"DEBUG RE Capture: {colors}")
			for color in colors:
				sanitize = sanitize.replace(color, "")
			print(sanitize)
			bag_color = colors[0].replace(" ", "").replace("bag:", "") if len(colors) > 0 else "None"
			print(bag_color)
			response = requests.get(f"{synthesizer_url}{sanitize}")
			print(response)
			# Send the response to the websocket.
			asyncio.run(websocket.send_ws_message("127.0.0.1:25000", f'color:{bag_color}'))

	except sr.RequestError as e:
		print("Could not request results; {0}".format(e))
	except sr.UnknownValueError as e:
		print("Unknown error occurred {0}".format(e))


if __name__ == "__main__":
	stt_to_tts()