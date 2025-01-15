import sys
import os
sys.path.insert(0, os.getcwd()+'/glados_tts')

import torch
import requests
from utils.tools import prepare_text
from scipy.io.wavfile import write
import time

print("\033[1;94mINFO:\033[;97m Initializing TTS Engine...")

# Select the device
if torch.is_vulkan_available():
	device = 'vulkan'
if torch.cuda.is_available():
	device = 'cuda'
else:
	device = 'cpu'

# Load models
if __name__ == "__main__":
	glados = torch.jit.load('./models/glados.pt')
	vocoder = torch.jit.load('./models/vocoder-gpu.pt', map_location=device)
else:
	glados = torch.jit.load('glados_tts/models/glados.pt')
	vocoder = torch.jit.load('glados_tts/models/vocoder-gpu.pt', map_location=device)

# Prepare models in RAM
for i in range(4):
	init = glados.generate_jit(prepare_text(str(i)))
	init_mel = init['mel_post'].to(device)
	init_vo = vocoder(init_mel)


def glados_tts(text):

	# Tokenize, clean and phonemize input text
	x = prepare_text(text).to('cpu')

	with torch.no_grad():

		# Generate generic TTS-output
		old_time = time.time()
		tts_output = glados.generate_jit(x)

		# Use HiFiGAN as vocoder to make output sound like GLaDOS
		mel = tts_output['mel_post'].to(device)
		audio = vocoder(mel)
		print("\033[1;94mINFO:\033[;97m The audio sample took " + str(round((time.time() - old_time) * 1000)) + " ms to generate.")

		# Normalize audio to fit in a wav-file
		audio = audio.squeeze()
		audio = audio * 32768.0
		audio = audio.cpu().numpy().astype('int16')
		output_file = './audio/GLaDOS-tts-temp-output.wav'

		# Write audio file to disk
		# 22,05 kHz sample rate 
		write(output_file, 22050, audio)

	return True


def post_sound(sound_file):
	url = 'http://10.8.0.6:8000/upload_sound'
	with open(sound_file, 'rb') as file:
		resp = requests.post(url=url, files={'file': file})
		print("\033[1;94mINFO:\033[;97m Response from REST API: ", (resp.json().get('message') if resp is not None else 'Failed'))


# If the script is run directly, assume a remote engine
if __name__ == "__main__":
	# Remote Engine Variables
	PORT = 8124

	from flask import Flask, request
	import urllib.parse

	print("\033[1;94mINFO:\033[;97m Initializing TTS Server...")
	
	app = Flask(__name__)

	@app.route('/synthesize/', defaults={'text': ''})
	@app.route('/synthesize/<path:text>')
	def synthesize(text):
		if text == '': return 'No input'
		line = urllib.parse.unquote(request.url[request.url.find('synthesize/')+11:])
		# Generate New Sample
		print("\033[1;94mINFO:\033[;97m Generating new sample from text: " + line)
		if glados_tts(line.replace('+', ' ')):
			tempfile = os.getcwd()+'/audio/GLaDOS-tts-temp-output.wav'
			print("\033[1;94mINFO:\033[;97m Sending sound to REST API...")
			print("\033[1;94mINFO:\033[;97m Sound file: " + tempfile)
			post_sound(tempfile)
			os.remove(tempfile)
			return 'TTS Engine Success'
		else:
			return 'TTS Engine Failed'
			
	cli = sys.modules['flask.cli']
	cli.show_server_banner = lambda *x: None
	app.run(host="0.0.0.0", port=PORT)
