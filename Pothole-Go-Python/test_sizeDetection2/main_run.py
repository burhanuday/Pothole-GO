from test_sizeDetection2.test1_objectParameters2 import ComputerVision
import sys, json, os


def read_line():
    lines = sys.stdin.readlines()
    return json.loads(lines[0])


def main():
    images = read_line()  # reads the images from json

    cv = ComputerVision()
    cwd = os.getcwd()
    file_all = os.listdir(cwd)

    for f in file_all:
        if f.lower().endswith('jpg'): images.append(f)  # checks for all files with the given extension

    for i in images:
        image = i
        cv.measure_object_dimension(image, coin_diameter=24, unit='mm')  # mm measure


if __name__ == '__main__':
    main()

