from test_sizeDetection.test3_objectParameters import final
from test_sizeDetection.measurement_pydo import ComputerVision

import sys, json, os

def read_line():
    print("Hello World")
    lines = sys.stdin.readlines()
    return json.loads(lines[0])

def main():
    images = read_line() # reads the images from json
    print(images)

    cv = ComputerVision()
    cwd = os.getcwd()
    file_all = os.listdir(cwd)
    final()

    for f in file_all:
        if f.lower().__contains__('jpg'): images.append(f)  # checks for all files with the given extension

    for i in images:
        image = i
        cv.measure_object_dimension(image, coin_diameter=24, unit='mm')  # mm measure



if __name__ == '__main__':
    main()
    final()
