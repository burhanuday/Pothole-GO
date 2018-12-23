from __future__ import print_function, division
from builtins import input
import imutils
import numpy as np
import cv2
from skimage import feature
from skimage import io
from skimage.color import rgb2gray
from skimage import img_as_ubyte


class Utilities:
    def __init__(self):
        print('Checks each and every file')

    def optimize_image(self, filename, resize_width, rotate_angle, blur):
        image = cv2.imread(filename)
        # image = image[:, :, ::-1]  # convert from BGR to RGB image for skimage
        image = imutils.resize(image, width=resize_width)
        image = imutils.rotate(image, angle=rotate_angle)
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        gray = cv2.GaussianBlur(gray, blur, 0)
        cv2.imshow('Gray', gray)
        return image, gray

    def detect_edge(self, gray, cannyMin, cannyMax):  # changed from image to gray
        edged1 = feature.canny(gray, sigma=3)  # changed to skimage and sigma values
        edged = img_as_ubyte(edged1)  # to convert from skimage to cv2 image
        edged = cv2.Canny(edged, cannyMin, cannyMax)  # changed from gray to edged
        edged = cv2.dilate(edged, None, iterations=1)  # try playing with iterations
        edged = cv2.erode(edged, None, iterations=1)
        cv2.imshow('Edged', edged)
        return edged

    def detect_and_sort_objects(self, image):
        from imutils import contours

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
        return box, orig

    def mark_corners(self, box, image):
        for (x, y) in box:
            cv2.circle(image, (int(x), int(y)), 3, (0, 0, 255), -1)

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
