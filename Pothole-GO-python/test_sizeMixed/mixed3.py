from __future__ import print_function, division
from builtins import input
import imutils
import numpy as np
import os
from skimage import feature
from skimage import io
from skimage.color import rgb2gray
from skimage import img_as_ubyte
from imutils import contours
import urllib.request
import urllib.parse
import base64
import json
import urllib
import os.path
import cv2
import sys


class Utilities:
    def __init__(self):
        print('Processing...')

    def optimize_image(self, filename, resize_width, rotate_angle, blur):
        image = cv2.imread(filename)
        image = imutils.resize(image, width=resize_width)
        image = imutils.rotate(image, angle=rotate_angle)
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        gray = cv2.GaussianBlur(gray, blur, 0)
        cv2.imshow('Gray', gray)
        print("Optimize Image(image): ", type(image))
        return image, gray

    def detect_edge(self, gray, cannyMin, cannyMax):
        edged1 = feature.canny(gray, sigma=3)  # changed to skimage and sigma values
        edged = img_as_ubyte(edged1)  # to convert from skimage to cv2 image
        edged = cv2.Canny(edged, cannyMin, cannyMax)  # changed from gray to edged
        edged = cv2.dilate(edged, None, iterations=1)  # try playing with iterations
        edged = cv2.erode(edged, None, iterations=1)
        cv2.imshow('Edged', edged)
        print("Edged: ", type(edged))
        return edged

    def detect_and_sort_objects(self, image):
        cnts = cv2.findContours(image.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        cnts = cnts[0] if imutils.is_cv2() else cnts[1]
        (cnts, _) = contours.sort_contours(cnts)
        return cnts

    def create_bounding_box(self, image, target_object, draw=True):
        from imutils import perspective

        orig = image.copy()
        box = cv2.minAreaRect(target_object)
        box = cv2.cv.BoxPoints(box) if imutils.is_cv2() else cv2.boxPoints(box)
        box = np.array(box, dtype='int')

        '''
        order the points in the object such that they appear in top-left, top-right,
        bottom-right, and bottom-left order, then draw the outline of the rotated
        bounding box
        '''
        box = perspective.order_points(box)
        if draw == True: cv2.drawContours(orig, [box.astype('int')], -1, (0, 255, 0), 1)
        print("Orig: ", type(orig))
        return box, orig

    def mark_corners(self, box, image):
        for (x, y) in box:
            cv2.circle(image, (int(x), int(y)), 3, (0, 0, 255), -1)
            print("Type in Utilities class: ", type(image))

    def get_midpoints(self, box, image, draw=True):
        def midpoint(ptA, ptB):
            return ((ptA[0] + ptB[0]) * 0.5, (ptA[1] + ptB[1]) * 0.5)

        # unpack the ordered bounding box
        (tl, tr, br, bl) = box

        # compute the midpoint between the top-left and top-right, followed by the midpoint between bottom-left and bottom-right
        (tltrX, tltrY) = midpoint(tl, tr)
        (blbrX, blbrY) = midpoint(bl, br)

        # compute the midpoint between the top-left and bottom-left points, followed by the midpoint between the top-right and bottom-right
        (tlblX, tlblY) = midpoint(tl, bl)
        (trbrX, trbrY) = midpoint(tr, br)

        if draw:
            # draw the midpoints on the image
            cv2.circle(image, (int(tltrX), int(tltrY)), 3, (255, 0, 0), -1)
            cv2.circle(image, (int(blbrX), int(blbrY)), 3, (255, 0, 0), -1)
            cv2.circle(image, (int(tlblX), int(tlblY)), 3, (255, 0, 0), -1)
            cv2.circle(image, (int(trbrX), int(trbrY)), 3, (255, 0, 0), -1)

            # draw lines between the midpoints
            cv2.line(image, (int(tltrX), int(tltrY)), (int(blbrX), int(blbrY)), (255, 0, 255), 1)
            cv2.line(image, (int(tlblX), int(tlblY)), (int(trbrX), int(trbrY)), (255, 0, 255), 1)

        return tltrX, tltrY, blbrX, blbrY, tlblX, tlblY, trbrX, trbrY

    def get_distances(self, tltrX, tltrY, blbrX, blbrY, tlblX, tlblY, trbrX, trbrY):
        from scipy.spatial import distance as dist
        dA = dist.euclidean((tltrX, tltrY), (blbrX, blbrY))
        dB = dist.euclidean((tlblX, tlblY), (trbrX, trbrY))
        return dA, dB

    def get_dimensions(self, dA, dB, ratio, image, unit, tltrX, tltrY, trbrX, trbrY):
        dimA = dA / ratio
        dimB = dB / ratio
        # draw the dimensions on the image
        cv2.putText(image, "{:.1f}{}".format(dimA, unit), (int(tltrX - 15), int(tltrY - 10)),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
        cv2.putText(image, "{:.1f}{}".format(dimB, unit), (int(trbrX + 10), int(trbrY)),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
        print("get_dimensions: ", type(image))
        # cv2.imwrite("E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_sizeMixed/processed_images/processed_final.jpg", image)
        # cv2.imwrite("E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_sizeMixed/processed2.jpg", image)

'''
def read_line():
    lines = sys.stdin.readlines()
    return json.loads(lines[0])
'''

get_url = str(input("Give URL: "))
# get_url = read_line()
name = get_url.split("/")[-1]
fullname = str(name) + ".jpg"

def download_image():
    # save_path = "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_sizeDetection2/downloaded_images/"
    save_path = "../test_sizeMixed/downloaded_images/"
    print(save_path)
    print(fullname)
    s = urllib.request.urlretrieve(get_url, os.path.join(os.path.dirname(__file__), save_path) + "{:s}".format(str(fullname)))

    # def save_processed_img():
    # path = "./test_sizeMixed/processed_images"
    # print(path)
    # cv2.imwrite(os.path.join(path, "dfjd.jpg"), s)

    # save_processed_img()


download_image()


class ComputerVision(Utilities):
    def __init__(self):
        Utilities.__init__(self)
        self.utils = Utilities()
        self.pixelsPerMetric = None

    def measure_object_dimension(self, image, coin_diameter, unit,
                                 resize_width, rotate_angle, blur, cannyMin, cannyMax,
                                 edge_iterations):  # updated on Feb 3rd, 2017

        utils = self.utils
        pixelsPerMetric = self.pixelsPerMetric

        # I. GET ALL OBJECTS IN THE IMAGE
        # step I.1: load the image, convert it to grayscale, and blur it slightly
        resized, blurred = utils.optimize_image(image, resize_width, rotate_angle, blur)

        # step I.2: perform edge detection, then perform a dilation + erotion to close gaps in between object edges
        edge = utils.detect_edge(blurred, cannyMin, cannyMax)

        # step I.3: find and sort objects (sort from left-to-right)
        objs = utils.detect_and_sort_objects(edge)

        # II. LOOP OVER THE OBJECTS IDENTIFIED
        for obj in objs:
            # step II.1: compute the bounding box of the object and draw the box (rectangle)
            box, original_image = utils.create_bounding_box(resized, obj)

            # step II.2: mark the corners of the box
            utils.mark_corners(box, original_image)

            # step II.3: compute the midpoints and mark them
            tltrX, tltrY, blbrX, blbrY, tlblX, tlblY, trbrX, trbrY = utils.get_midpoints(box, original_image)

            # step II.4: compute the Euclidean distance between the midpoints
            dA, dB = utils.get_distances(tltrX, tltrY, blbrX, blbrY, tlblX, tlblY, trbrX, trbrY)

            # step II.5: perform the calibration pixel to millimeters if the pixels per metric has not been initialized
            if pixelsPerMetric is None: pixelsPerMetric = dB / coin_diameter

            # step II.6: compute the dimension of the object and show them on the image
            utils.get_dimensions(dA, dB, pixelsPerMetric, original_image, unit, tltrX, tltrY, trbrX, trbrY)

            cv2.imshow(image, original_image)
            print("Type in CompVis Class(original_image): ", type(original_image))
            print("Type in CompVis Class(image): ", type(image))
            cv2.waitKey(0)

        cv2.destroyAllWindows()


cv = ComputerVision()


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

# p = 'parameter_file.txt'
# d = get_parameters_from_txt(os.path.join(os.path.dirname(__file__), p))
d = get_parameters_from_txt('parameter_file.txt')

wd = os.path.join(d['directory'])  # new
file_all = os.listdir(wd)

images = []
for f in file_all:
    if any(valid_file_extension.lower() in f.lower() for valid_file_extension in
           d['image_file_extensions']): images.append(f)

for i in images:
    image = os.path.join(d['directory'], i)
    print("i in images type: ", type(image))
    cv.measure_object_dimension(image, coin_diameter=int(d['coin_diameter']), unit=d['unit'],
                                resize_width=int(d['resize_width']), rotate_angle=int(d['rotate_angle']),
                                blur=(int(d['blur']), int(d['blur'])),
                                cannyMin=int(d['cannyMin']), cannyMax=int(d['cannyMax']),
                                edge_iterations=int(d['edge_iterations']))
    print("Reached here...")

# https://storage.googleapis.com/potholego.appspot.com/2018-12-23%2018.30.44.jpg_1545570112694
# https://storage.googleapis.com/potholego.appspot.com/Qe7ik9D.jpg_1545539333821
# https://storage.googleapis.com/potholego.appspot.com/S1.png_1545539333813
# directory = /Users/idrees/Documents/Projects/extra-projects/DeepBlue/Pothole-GO-Server/Pothole-GO-Python/test_sizeMixed/parameter_file.txt

