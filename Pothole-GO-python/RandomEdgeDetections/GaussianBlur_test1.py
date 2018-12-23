import cv2
import numpy as np
import imutils

img = cv2.imread('1_test_pothole.jpg', 1)

'''
height, width = img.shape[1:1]

resized = cv2.resize(img, (int(height/10), int(width/10)), interpolation = cv2.INTER_AREA)
gray = cv2.cvtColor(resized, cv2.COLOR_BGR2GRAY)
blurred_gray = cv2.GaussianBlur(gray, (5, 5), 0)
edged = cv2.Canny(blurred_gray, 75, 200)
cv2.imshow("Edged", edged)
cv2.waitKey(0)
'''


def image_resize(image, width=None, height=None, inter=cv2.INTER_AREA):
    # initialize the dimensions of the image to be resized and
    # grab the image size
    dim = None
    (h, w) = image.shape[:2]

    # if both the width and height are None, then return the
    # original image
    if width is None and height is None:
        return image

    # check to see if the width is None
    if width is None:
        # calculate the ratio of the height and construct the
        # dimensions
        r = height / float(h)
        dim = (int(w * r), height)

    # otherwise, the height is None
    else:
        # calculate the ratio of the width and construct the
        # dimensions
        r = width / float(w)
        dim = (width, int(h * r))

    # resize the image
    resized = cv2.resize(image, dim, interpolation=inter)

    # return the resized image
    return resized


rsz_image = image_resize(img, height=512, width=512)
cv2.imshow('original image', img)
cv2.imshow('resized image', rsz_image)

gray = cv2.cvtColor(rsz_image, cv2.COLOR_BGR2GRAY)
blurred_gray = cv2.GaussianBlur(gray, (5, 5), 0)
edged = cv2.Canny(blurred_gray, 75, 200)
cv2.imshow("Edged", edged)


cv2.waitKey(0)