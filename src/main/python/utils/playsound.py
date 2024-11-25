import time
import wave
import pyaudio


def playsound(sound_path: str):
	wf = wave.open(rf"{sound_path}", 'rb')
	# instantiate PyAudio
	p = pyaudio.PyAudio()

	# define callback
	def callback(in_data, frame_count, time_info, status):
		data = wf.readframes(frame_count)
		return data, pyaudio.paContinue

	# open stream using callback
	stream = p.open(format=p.get_format_from_width(wf.getsampwidth()),
	                channels=wf.getnchannels(),
	                rate=wf.getframerate(),
	                output=True,
	                output_device_index = 1,
	                stream_callback=callback)
	# start the stream
	stream.start_stream()
	while stream.is_active():
		time.sleep(0.1)
	stream.close()
	wf.close()
	p.terminate()
