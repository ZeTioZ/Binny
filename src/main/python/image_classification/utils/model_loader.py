import torch

from ultralytics import YOLO


class ModelLoader:
	def __init__(self, model_path):
		self.model = YOLO(model_path)
		self.predictions = None

	def predict(self, image, classes=None, img_size=(640, 640), device: str | None = None):
		"""
		Predicts the classes of the given image.

		:param image: The image to predict the classes of.
		:param classes: The classes to predict.
		:param img_size: The size of the image.
		:param device: The device to use for the prediction.
		:return: The predictions of the model for the given image.
		"""
		if device is None:
			device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
		self.predictions = self.model(image, classes=classes, device=device, imgsz=img_size, verbose=False)
		return self.predictions