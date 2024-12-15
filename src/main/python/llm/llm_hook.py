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
import subprocess

import sys; sys.path.append(os.path.dirname(os.path.realpath(__file__)))

from wakeword.wakeword import Wakeword

llm_url = "http://127.0.0.1:1234/v1/chat/completions"
synthesizer_url = "http://127.0.0.1:8124/synthesize"
rest_api_url = "http://127.0.0.1:8000"
websocket_url = "127.0.0.1:25000"
current_task = None


def send_request(message, hand_context=None):
	hand_context = f"a {hand_context} object" if hand_context is not None else "nothing"
	payload = {
		"model": "hermes-3-llama-3.2-3b",
		"messages": [
			{ "role": "system", "content": f"You are a trash bin named \"Binny\" that always try to be funny and kid friendly. The user can ask where to place trash and you will have to answer according to these rules: plastic, metal and drink carton into the blue bin, food scraps in green bin, miscellaneous (which can contain paper, packagings, tissues) into black bin and styrofoam, cardboard or glass in none of these bins. The user can also ask you to open the different colored bins in that case, just answer that you can open the colored bin he wants and give that color as for the bin color chosen. If the user doesn't want to trash something respond with the color bin \"none\". For this session, the user is showing you at the camera {hand_context}." },
			{ "role": "user", "content": message }
		],
		"temperature": 0.7,
		"max_tokens": -1,
		"response_format": {
			"type": "json_schema",
			"json_schema": {
				"name": "binny_response",
				"strict": "true",
				"schema": {
					"type": "object",
					"properties": {
						"message": {
							"type": "string"
						},
						"bin_color": {
							"type": "string"
						}
					},
					"required": ["message", "bin_color"]
				}
			}
		},
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
	json_structured_response = json.loads(bot_response)
	sanitize = json_structured_response["message"].replace("\n", " ").replace("\r", "").replace("\t", "").replace("\\", "")
	sanitize = re.sub(r"\*([^*]+)\*", '', sanitize, flags=re.IGNORECASE)
	bag_color = json_structured_response["bin_color"] if json_structured_response.get("bin_color") else "none"
	asyncio.run(websocket.send_ws_message(f"{websocket_url}", f'color:{bag_color.lower()}'))
	print("Color:", bag_color)
	print("Sanitized bot response:", sanitize)
	response = asyncio.run(fetch_synthesizer_response(f"{synthesizer_url}/{sanitize}"))
	print("Synthesizer response:", response)


async def stt_to_tts():
	global current_task
	recognizer = sr.Recognizer()
	while True:
		if Wakeword(model_path="wakeword/models/binny_ww_v2.onnx", inference_framework="onnx").run():
			requests.get(url=f"{rest_api_url}/stop_sound")
			print("Keyword detected.")
			if current_task is not None and type(current_task) is threading.Thread:
				current_task.join()
				current_task = None
			await fetch_synthesizer_response(f"{synthesizer_url}/Hey%2C%20what%20can%20I%20do%20for%20you%3F")
			image_response = await post_image()
			hand_context = image_response.json().get('message') if image_response is not None else None
			hand_context = hand_context if hand_context is not None and hand_context in ['e-waste', 'general_waste', 'glass', 'metal', 'organic_waste', 'paper', 'plastic'] else None
			if hand_context in ['e-waste', 'general_waste', 'glass', 'metal', 'organic_waste', 'paper', 'plastic']:
				print(f"Hand context recognized: {hand_context}")
				user_input = f"As you can see, I have a {image_response.json().get('message')} object to throw away. Where should I put it?"
			else:
				with sr.Microphone() as source:
					recognizer.adjust_for_ambient_noise(source, duration=0.5)
					while True:
						try:
							print("Hand context not recognized. Going back to listening for a command...")
							audio2 = recognizer.listen(source)
							user_input = recognizer.recognize_google(audio2, language="en-US")
							break
						except sr.RequestError as e:
							print("Could not request results; {0}".format(e))
						except sr.UnknownValueError as e:
							print("Unknown error occurred {0}".format(e))
						except Exception as e:
							print("An error occurred: ", e)
			if user_input is not None:
				print(user_input)
				current_task = threading.Thread(target=process_command, args=(user_input, hand_context,))
				current_task.start()


async def post_image():
	if os.name == 'nt':
		cap = cv2.VideoCapture(0)
		ret, frame = cap.read() if cap.isOpened() else (False, None)
		cap.release()
		cv2.imwrite('image.jpg', frame)
	else:
		subprocess.check_call('fswebcam --device /dev/video0 image.jpg', shell=True)
	url = f'{rest_api_url}/upload_image'
	with open('image.jpg', 'rb') as file:
		resp = requests.post(url=url, files={'file': file})
		print("\033[1;94mINFO:\033[;97m Response from REST API: ", (resp.json().get('message') if resp is not None else 'Failed'))
	os.remove('image.jpg')
	return resp

if __name__ == "__main__":
	asyncio.run(stt_to_tts())