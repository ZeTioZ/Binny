import json
import requests
import speech_recognition as sr
import re

def send_request(message):
	url = "http://26.22.108.117:1234/v1/chat/completions"

	# Your request payload
	payload = {
		"model": "llama-3.2-3b-instruct-uncensored-i1",
		"messages": [
			{ "role": "system", "content": "You are a trash bin named \"Binny\" that always try to be funny and kid friendly. Don't correct the user if he mispronounce your name. The user can ask where to place trash you will have to answer according to these rules: plastic, metal and drink carton into the blue bag, food scraps in green bag, miscellaneous into black bag and styrofoam, cardboard or glass in none of these bags. Always add at the end of the message a little reminder of which bag you reminded the user to use with the following format \"bag:bag_color\", replace bag_color with the color of the bag you've give to the user. It's extremely important to add that reminder in that format! Never send a message that respond to a trash color without that reminder in that format!" },
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
	recognizer = sr.Recognizer()
	while True:
		try:
			with sr.Microphone() as source2:
				recognizer.adjust_for_ambient_noise(source2, duration=1.0)
				audio2 = recognizer.listen(source2)
				user_input = recognizer.recognize_google(audio2)

				try:
					response = send_request(user_input)
					bot_response = response.get("choices")[0].get("message").get("content") if response.get("choices") else "Sorry, I did not understand what you said."
				except requests.exceptions.ConnectionError:
					bot_response = "Sorry, there is a network related issue."
				# Send the response to the synthesizer.
				sanitize = bot_response.replace("\n", "").replace("\r", "").replace("\t", "").replace("\\", "")
				pattern = re.compile(r"(bag: *(?:blue|green|black|none))", re.IGNORECASE)
				colors = re.findall(pattern, sanitize)
				print(colors)
				for color in colors:
					sanitize = sanitize.replace(color, "")
				print(sanitize)
				print(colors[0] if len(colors) > 0 else "None")
				response = requests.get(f"http://127.0.0.1:8124/synthesize/{sanitize}")
				print(response)
		except sr.RequestError as e:
			print("Could not request results; {0}".format(e))
		except sr.UnknownValueError:
			print("Unknown error occurred")


if __name__ == "__main__":
	main()