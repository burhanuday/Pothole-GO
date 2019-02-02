import os

os.getcwd()
collection = "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_ML/potholeCount2/Datasets/pothole_test"
for i, filename in enumerate(os.listdir(collection)):
    os.rename(
        "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_ML/potholeCount2/Datasets/pothole_test/" +
        filename,
        "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_ML/potholeCount2/Datasets/pothole_test/" +
        str(i) + ".jpg")
