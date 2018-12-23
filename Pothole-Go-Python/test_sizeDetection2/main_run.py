from test_sizeDetection2.measurement_pydo2 import ComputerVision
import sys, json, os
import urllib.parse
import urllib.request
import urllib
import os.path
from pprint import pprint


def read_line():
    lines = sys.stdin.readlines()
    return json.loads(lines[0])


def download_image():
    save_path = "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_sizeDetection2/downloaded_images/"
    # get_url = str(input("Give URL: "))
    get_url = read_line()
    name = get_url.split("/")[-1]
    fullname = str(name) + ".jpg"
    urllib.request.urlretrieve(get_url, save_path + "{:s}".format(str(fullname)))


if __name__ == '__main__':
    download_image()