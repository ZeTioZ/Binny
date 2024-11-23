import os


def get_parent_path(path: str, level: int = 1) -> str:
	"""Return the parent path of the path."""
	for _ in range(level):
		path = os.path.dirname(path)
	return path


root_path = get_parent_path(__file__, 2)
join_char = os.path.sep
uploads_path = f'{root_path}{join_char}tts-rest-server{join_char}uploads{join_char}'