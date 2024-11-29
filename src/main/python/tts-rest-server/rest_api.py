import shutil
import os

from starlette.responses import RedirectResponse

from .__init__ import uploads_path
from typing import Optional
from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from ..utils.playsound import PlaySound

app = FastAPI()
audio = PlaySound()


# CORS settings (see https://fastapi.tiangolo.com/tutorial/cors)
origins = [
	"http://127.0.0.1:8124"
]

app.add_middleware(
	CORSMiddleware,
	allow_origins=origins,
	allow_credentials=True,
	allow_methods=["*"],
	allow_headers=["*"],
)


@app.get("/")
async def root():
	response = RedirectResponse(url='/docs')
	return response


@app.get("/stop_sound")
async def stop_sound():
	audio.stop_sound()
	return JSONResponse(content={"message": "Sound stopped!"}, status_code=200)


@app.post("/upload_sound")
async def upload_sound(file: Optional[UploadFile] = File(None)):
	if not os.path.exists(uploads_path):
		os.mkdir(uploads_path)
	try:
		print(f"{uploads_path}{file.filename}")
		with open(f"{uploads_path}{file.filename}", 'wb') as f:
			shutil.copyfileobj(file.file, f)
	except FileNotFoundError as error:
		return JSONResponse(content={"message": f"There was an error uploading the file(s)!\n{error}"}, status_code=500)
	finally:
		file.file.close()
	try:
		audio.stop_sound()
		audio.play_sound(f"{uploads_path}{file.filename}")
	except Exception as error:
		return JSONResponse(content={"message": f"There was an error playing the file(s)!\n{error}"}, status_code=500)
	# finally:
	# 	os.remove(f"{uploads_path}{file.filename}")
	return JSONResponse(content={"message": f"Successfully uploaded {file.filename}!"}, status_code=200)


@app.post("/upload_image")
async def upload_image(file: Optional[UploadFile] = File(None)):
	if not os.path.exists(uploads_path):
		os.mkdir(uploads_path)
	try:
		print(f"{uploads_path}{file.filename}")
		with open(f"{uploads_path}{file.filename}", 'wb') as f:
			shutil.copyfileobj(file.file, f)
	except FileNotFoundError as error:
		return JSONResponse(content={"message": f"There was an error uploading the file(s)!\n{error}"}, status_code=500)
	finally:
		file.file.close()
	return JSONResponse(content={"message": f"Successfully uploaded {file.filename}!"}, status_code=200)