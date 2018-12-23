from test_sizeDetection2.measurement_pydo2 import ComputerVision
import os
import sys, json, os


def get_parameters_from_txt(txt_file):
    d = dict()  # initializing an empty dictionary
    with open(txt_file) as f:
        content = f.readlines()
        for s in content:
            temp = s.split('=')
            if not ',' in temp[1]:
                d[temp[0].strip()] = temp[1].strip()
            else:
                lnew = []
                l = temp[1].split(',')
                for item in l:
                    item = item.strip()
                    lnew.append(item)
                d[temp[0].strip()] = lnew
    return d  # return a dictionary with the predetermined parameters


cv = ComputerVision()
d = get_parameters_from_txt('parameters.txt')

wd = os.path.join(d['directory'])  # new
file_all = os.listdir(wd)  # new 

images = []
for f in file_all:
    if any(valid_file_extension.lower() in f.lower() for valid_file_extension in
           d['image_file_extensions']): images.append(f)  # new

for i in images:
    image = os.path.join(d['directory'], i)  # new on Feb 3rd, 2017
    cv.measure_object_dimension(image, coin_diameter=int(d['coin_diameter']), unit=d['unit'],
                                resize_width=int(d['resize_width']), rotate_angle=int(d['rotate_angle']),
                                blur=(int(d['blur']), int(d['blur'])),
                                cannyMin=int(d['cannyMin']), cannyMax=int(d['cannyMax']),
                                edge_iterations=int(d['edge_iterations']))  # new
