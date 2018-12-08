from test_sizeDetection.measurement_pydo import ComputerVision
import os
import sys, json, numpy as np

class final:

    cv = ComputerVision()

    cwd = os.getcwd()
    file_all = os.listdir(cwd)

    def read_line(self):
        lines = sys.stdin.readlines()
        return json.loads(lines[0])

    images = read_line()

    for f in file_all:
        if f.lower().endswith('jpg'): images.append(f)  # checks for all files with the given extension

    for i in images:
        image = i
        cv.measure_object_dimension(image, coin_diameter=24, unit='mm')  # mm measure
