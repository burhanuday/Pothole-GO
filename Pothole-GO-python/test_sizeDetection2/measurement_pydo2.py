from test_sizeDetection2.cv_utilities2 import Utilities as utils
import cv2
import os, sys
import urllib.request
import urllib.parse
import base64

class ComputerVision:
    def __init__(self):
        self.utils = utils()
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
            print(type(image))
            # print("Encoded Image")
            # encoded = cv2.imencode('.jpg', original_image)[1].tostring()
            # print(encoded)
            # cv2.imwrite("../test_sizeDetection2/processed_images/wakanda.jpg", image)
            cv2.waitKey(0)

        cv2.destroyAllWindows()
