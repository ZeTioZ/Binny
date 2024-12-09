import asyncio
import json
import requests
import speech_recognition as sr
import re
import aiohttp
import websocket
import threading
import cv2
import os
import time

cap = cv2.VideoCapture(0)
if cap.isOpened():
	cap.read()
llm_url = "http://127.0.0.1:1234/v1/chat/completions"
synthesizer_url = "http://127.0.0.1:8124/synthesize"
rest_api_url = "http://127.0.0.1:8000"
websocket_url = "10.8.0.6:25000"
current_task = None


def send_request(message, hand_context=None):
	hand_context = f"a {hand_context} object" if hand_context is not None else "nothing"
	payload = {
		"model": "llama-3.2-3b-instruct-uncensored-i1",
		"messages": [
			{ "role": "system", "content": f"You are a trash bin named \"Binny\" that always try to be funny and kid friendly. Don't correct the user if he mispronounce your name. The user can ask where to place trash you will have to answer according to these rules: plastic, metal and drink carton into the blue bag, food scraps in green bag, miscellaneous (which can contain paper, packagings, tissues) into black bag and styrofoam, cardboard or glass in none of these bags. Always add at the end of the message a little reminder of which bag you reminded the user to use with the following format \"bag:bag_color\", replace bag_color with the color of the bag you've give to the user. It's extremely important to add that reminder in that format! Never send a message that respond to a trash color without that reminder in that format! For this session, the user is showing you {hand_context}." },
			{ "role": "user", "content": message }
		],
		"temperature": 0.7,
		"max_tokens": -1,
		"stream": False
	}

	headers = {
		"Content-Type": "application/json"
	}
	try:
		response = requests.post(llm_url, headers=headers, data=json.dumps(payload))
		if response.status_code == 200:
			return response.json()
		else:
			return {"error": "Request failed with status code " + str(response.status_code)}
	except Exception as e:
		return {"Error while sending request to the LLM model": str(e)}


async def fetch_synthesizer_response(url):
	async with aiohttp.ClientSession() as session:
		async with session.get(url) as response:
			return await response.text()

def process_command(user_input, hand_context=None):
	print("Processing command...")
	response = send_request(user_input, hand_context)
	bot_response = response.get("choices")[0].get("message").get("content") if response.get("choices") else "Sorry, it seems that there is an issue with my brain. Please try again later."
	sanitize = bot_response.replace("\n", " ").replace("\r", "").replace("\t", "").replace("\\", "")
	bag_pattern = re.compile(r"(bag: *(?:blue|green|black|none))", re.IGNORECASE)
	sanitize = re.sub(r"\*([^*]+)\*", '', sanitize, flags=re.IGNORECASE)
	colors = re.findall(bag_pattern, sanitize)
	sanitize = re.sub(bag_pattern, '', sanitize)
	bag_color = colors[0].replace(" ", "").replace("bag:", "") if len(colors) > 0 else "None"
	asyncio.run(websocket.send_ws_message(f"{websocket_url}", f'color:{bag_color.lower()}'))
	print("Sanitized bot response: ", sanitize)
	response = asyncio.run(fetch_synthesizer_response(f"{synthesizer_url}/{sanitize}"))
	print("Synthesizer response: ", response)


async def stt_to_tts():
	global current_task
	keyword_variants = ["hey", "minnie", "bunny", "binny", "benny", "beanies", "ben", "benn", "beanie", "ebony", "baby", "binnie", "bin", "binn"]
	recognizer = sr.Recognizer()
	try:
		with sr.Microphone() as source2:
			recognizer.adjust_for_ambient_noise(source2, duration=0.5)
			while True:
				try:
					print("Listening for keyword...")
					audio2 = recognizer.listen(source2)
					user_input = recognizer.recognize_google(audio2)
					print(user_input)
					keyword_found = False
					for keyword in keyword_variants:
						if keyword in user_input.lower():
							keyword_found = True
							break
					if keyword_found:
						print("Keyword detected. Listening for command...")
						requests.get(url=f"{rest_api_url}/stop_sound")
						if current_task is not None:
							current_task.join()
							current_task = None
						current_task = await fetch_synthesizer_response(f"{synthesizer_url}/Hey%2C%20what%20can%20I%20do%20for%20you%3F")
						ret, frame = cap.read() if cap.isOpened() else (False, None)
						image_response = await post_image(frame) if frame is not None else None
						hand_context = image_response.json().get('message') if image_response is not None else None
						hand_context = hand_context if hand_context is not None and hand_context in ['e-waste', 'general_waste', 'glass', 'metal', 'organic_waste', 'paper', 'plastic'] else None
						if hand_context in ['e-waste', 'general_waste', 'glass', 'metal', 'organic_waste', 'paper', 'plastic']:
							user_input = f"As you can see, I have a {image_response.json().get('message')} object to throw away. Where should I put it?"
						else:
							time.sleep(2)
							audio2 = recognizer.listen(source2)
							user_input = recognizer.recognize_google(audio2)
						print(user_input)
						current_task = threading.Thread(target=process_command, args=(user_input, hand_context,))
						current_task.start()
				except sr.RequestError as e:
					print("Could not request results; {0}".format(e))
				except sr.UnknownValueError as e:
					print("Unknown error occurred {0}".format(e))
				except Exception as e:
					print("An error occurred: ", e)
	except sr.RequestError as e:
		print("Could not request results; {0}".format(e))
	except sr.UnknownValueError as e:
		print("Unknown error occurred {0}".format(e))


async def post_image(image_file):
	cv2.imwrite('image.jpg', image_file)
	url = f'{rest_api_url}/upload_image'
	with open('image.jpg', 'rb') as file:
		resp = requests.post(url=url, files={'file': file})
		print("\033[1;94mINFO:\033[;97m Response from REST API: ", (resp.json().get('message') if resp is not None else 'Failed'))
	os.remove('image.jpg')
	return resp

if __name__ == "__main__":
	asyncio.run(stt_to_tts())