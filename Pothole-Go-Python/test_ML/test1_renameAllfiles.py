import os

os.getcwd()
collection = "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_ML/pothole_burhan"
for i, filename in enumerate(os.listdir(collection)):
    os.rename(
        "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_ML/pothole_burhan/" +
        filename,
        "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_ML/pothole_burhan/" +
        str(i) + ".jpg")
