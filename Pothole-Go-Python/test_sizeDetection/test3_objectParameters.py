from potholeHeight_test1.test_sizeDetection.pydonesia import ComputerVision
import os

cv = ComputerVision()

cwd = os.getcwd()
file_all = os.listdir(cwd)

images = []
for f in file_all:
    if f.lower().endswith('jpg'): images.append(f)  # checks for all files with the given extension

for i in images:
    image = i
    cv.measure_object_dimension(image, coin_diameter=24, unit='mm')  # mm measure
