import os

os.getcwd()
collection = "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_ML/noPotholeImages"
for i, filename in enumerate(os.listdir(collection)):
    os.rename(
        "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_ML/noPotholeImages/" +
        filename,
        "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_ML/noPotholeImages/" +
        str(i) + ".jpg")
