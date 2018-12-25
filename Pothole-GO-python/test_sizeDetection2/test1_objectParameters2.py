from test_sizeDetection2.measurement_pydo2 import ComputerVision
from test_sizeDetection2.cv_utilities2 import Utilities
import os
import sys, json, os
import sys, json, os
import urllib.parse
import urllib.request
import urllib
import os.path
import cv2
import sys
from pprint import pprint

cv = ComputerVision()


def read_line():
    lines = sys.stdin.readlines()
    return json.loads(lines[0])


get_url = str(input("Give URL: "))
# get_url = read_line()
name = get_url.split("/")[-1]
fullname = str(name) + ".jpg"


def download_image():
    # save_path = "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_sizeDetection2/downloaded_images/"
    save_path = "../test_sizeDetection2/downloaded_images/"
    # get_url = str(input("Give URL: "))
    # get_url = read_line()
    # name = get_url.split("/")[-1]
    # fullname = str(name) + ".jpg"
    print(fullname)
    urllib.request.urlretrieve(get_url, save_path + "{:s}".format(str(fullname)))

    def get_parameters_from_txt(txt_file):
        d = dict()  # initializing empty dictionary
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

    d = get_parameters_from_txt('parameter_file.txt')

    wd = os.path.join(d['directory'])  # new
    file_all = os.listdir(wd)

    images = []
    for f in file_all:
        if any(valid_file_extension.lower() in f.lower() for valid_file_extension in
               d['image_file_extensions']): images.append(f)

    for i in images:
        image = os.path.join(d['directory'], i)
        cv.measure_object_dimension(image, coin_diameter=int(d['coin_diameter']), unit=d['unit'],
                                    resize_width=int(d['resize_width']), rotate_angle=int(d['rotate_angle']),
                                    blur=(int(d['blur']), int(d['blur'])),
                                    cannyMin=int(d['cannyMin']), cannyMax=int(d['cannyMax']),
                                    edge_iterations=int(d['edge_iterations']))
        print("Reached here...")
        writing = cv2.imwrite(fullname, image)
        print(writing)

        '''

        def save_processed_img():
            path = "../test_sizeDetection2/processed_images/"
            print(path)
            print(fullname)
            cv2.imwrite(os.path.join(path, fullname), image)

        save_processed_img()
        '''
'''
    def save_processed_img():
        path = "../test_sizeDetection2/processed_images/"
        print(path)
        print(fullname)
        cv2.imwrite(os.path.join(path, fullname), fullname)

    save_processed_img()

'''
download_image()

'''
def save_processed_file():
    dwnld_img_path = "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_sizeDetection2/downloaded_images/"
    path = 'E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_sizeDetection2/processed_images/'
    name = download_image().split("/")[-1]
    fullname_final = str(name) + "_processed.jpg"
    # cv2.imwrite(fullname, fullname)
    cv2.imwrite(os.path.join(path, download_image().fullname), fullname_final)

save_processed_file()
'''

'''
def get_parameters_from_txt(txt_file):
    d = dict()  # initializing empty dictionary
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


d = get_parameters_from_txt('parameter_file.txt')

wd = os.path.join(d['directory'])  # new
file_all = os.listdir(wd)

images = []
for f in file_all:
    if any(valid_file_extension.lower() in f.lower() for valid_file_extension in
           d['image_file_extensions']): images.append(f)

for i in images:
    image = os.path.join(d['directory'], i)
    cv.measure_object_dimension(image, coin_diameter=int(d['coin_diameter']), unit=d['unit'],
                                resize_width=int(d['resize_width']), rotate_angle=int(d['rotate_angle']),
                                blur=(int(d['blur']), int(d['blur'])),
                                cannyMin=int(d['cannyMin']), cannyMax=int(d['cannyMax']),
                                edge_iterations=int(d['edge_iterations']))

'''

# https://storage.googleapis.com/potholego.appspot.com/2018-12-23%2018.30.44.jpg_1545570112694

'''
get_url = str(input("Give URL: "))
            # get_url = read_line()
            name = get_url.split("/")[-1]
            fullname = str(name) + ".jpg"

            def download_image():
                # save_path = "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_sizeDetection2/downloaded_images/"
                save_path = "../test_sizeDetection2/downloaded_images/"
                # get_url = str(input("Give URL: "))
                # get_url = read_line()
                # name = get_url.split("/")[-1]
                # fullname = str(name) + ".jpg"
                print(fullname)
                urllib.request.urlretrieve(get_url, save_path + "{:s}".format(str(fullname)))

                def save_processed_img():
                    path = "../test_sizeDetection2/processed_images/"
                    print(path)
                    print(fullname)
                    cv2.imwrite(os.path.join(path, fullname), fullname)

                save_processed_img()

            download_image()
            '''