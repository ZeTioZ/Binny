import time
import wave
import pyaudio
import threading

class PlaySound:
	def __init__(self):
		self.audio = None
		self.stream = None
		self.thread = None
		self.wf = None

	def play_sound(self, sound_path: str):
		self.thread = threading.Thread(target=self._play, args=(sound_path,))
		self.thread.start()

	def _play(self, sound_path: str):
		self.audio = pyaudio.PyAudio()
		self.wf = wave.open(rf"{sound_path}", 'rb')

		# define callback
		def callback(in_data, frame_count, time_info, status):
			data = self.wf.readframes(frame_count)
			return data, pyaudio.paContinue

		# open stream using callback
		self.stream = self.audio.open(format=self.audio.get_format_from_width(self.wf.getsampwidth()),
		                              channels=self.wf.getnchannels(),
		                              rate=self.wf.getframerate(),
		                              output=True,
		                              stream_callback=callback)
		# start the stream
		self.stream.start_stream()
		while self.stream is not None and self.stream.is_active():
			time.sleep(0.1)
		self.stop_sound()

	def stop_sound(self) -> bool:
		if self.stream is not None:
			self.stream.stop_stream()
			self.stream.close()
			self.stream = None
		if self.wf is not None:
			self.wf.close()
			self.wf = None
		if self.audio is not None:
			self.audio.terminate()
			self.audio = None
		return True

	def stop(self):
		if self.thread and self.thread.is_alive():
			self.stop_sound()
			self.thread.join()