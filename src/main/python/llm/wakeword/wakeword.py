import pyaudio
import numpy as np
import openwakeword
from openwakeword.model import Model

openwakeword.utils.download_models()


class Wakeword:
	def __init__(self, chunk_size=1280, model_path="", inference_framework=""):
		self.chunk_size = chunk_size
		self.model_path = model_path
		self.inference_framework = inference_framework
		self.audio = pyaudio.PyAudio()
		self.mic_stream = self.audio.open(format=pyaudio.paInt16, channels=1, rate=16000, input=True, frames_per_buffer=self.chunk_size)
		self.owwModel = Model(wakeword_models=[self.model_path], inference_framework=self.inference_framework)
		self.n_models = len(self.owwModel.models.keys())


	def run(self):
		print("\n\n")
		print("#"*100)
		print("Listening for wakewords...")
		print("#"*100)
		print("\n"*(self.n_models*3))

		while True:
			data = self.mic_stream.read(self.chunk_size)
			data = np.frombuffer(data, dtype=np.int16)
			self.owwModel.predict(data)
			for mdl in self.owwModel.prediction_buffer.keys():
				scores = list(self.owwModel.prediction_buffer[mdl])
				if scores[-1] > 0.5:
					print(f"Model: {mdl} | Score: {scores[-1]} | Detected: {scores[-1] > 0.5}")
					self.mic_stream.stop_stream()
					return True
