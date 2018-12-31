from scipy.spatial import distance as dist
from imutils import perspective
from imutils import contours
import numpy as np
import argparse
import imutils
import cv2


def midpoint(ptA, ptB):
    return ((ptA[0] + ptB[0]) * 0.5, (ptA[1] + ptB[1]) * 0.5)


image = cv2.imread('rsz_pothole1.jpg')  # path to original image
cv2.imshow('Original', image)

gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
gray = cv2.GaussianBlur(gray, (7, 7), 0)
cv2.imshow('Grayscale', gray)

edged = cv2.Canny(gray, 50, 100)
edged = cv2.dilate(edged, None, iterations=1)
edged = cv2.erode(edged, None, iterations=1)
cv2.imshow('Edged', edged)

cnts = cv2.findContours(edged.copy(), cv2.RETR_EXTERNAL,
                        cv2.CHAIN_APPROX_SIMPLE)
cnts = cnts[0] if imutils.is_cv2() else cnts[1]

(cnts, _) = contours.sort_contours(cnts)
colors = ((0, 0, 255), (240, 0, 159), (0, 165, 255), (255, 255, 0),
          (255, 0, 255))
refObj = None

for c in cnts:
    if cv2.contourArea(c) < 100:
        continue

    box = cv2.minAreaRect(c)
    box = cv2.cv.BoxPoints(box) if imutils.is_cv2() else cv2.boxPoints(box)
    box = np.array(box, dtype="int")

    box = perspective.order_points(box)

    cX = np.average(box[:, 0])
    cY = np.average(box[:, 1])

    if refObj is None:
        (tl, tr, br, bl) = box
        (tlblX, tlblY) = midpoint(tl, bl)
        (trbrX, trbrY) = midpoint(tr, br)

        D = dist.euclidean((tlblX, tlblY), (trbrX, trbrY))
        refObj = (box, (cX, cY), D / 12.995)  # width parameter, needs to be changed
        continue

    orig = image.copy()
    cv2.drawContours(orig, [box.astype("int")], -1, (0, 255, 0), 2)
    cv2.drawContours(orig, [refObj[0].astype("int")], -1, (0, 255, 0), 2)

    refCoords = np.vstack([refObj[0], refObj[1]])
    objCoords = np.vstack([box, (cX, cY)])

    # loop over the original points
    for ((xA, yA), (xB, yB), color) in zip(refCoords, objCoords, colors):
        # draw circles corresponding to the current points and
        # connect them with a line
        cv2.circle(orig, (int(xA), int(yA)), 5, color, -1)
        cv2.circle(orig, (int(xB), int(yB)), 5, color, -1)
        cv2.line(orig, (int(xA), int(yA)), (int(xB), int(yB)),
                 color, 2)

        # compute the Euclidean distance between the coordinates,
        # and then convert the distance in pixels to distance in
        # units
        D = dist.euclidean((xA, yA), (xB, yB)) / refObj[2]
        (mX, mY) = midpoint((xA, yA), (xB, yB))
        cv2.putText(orig, "{:.1f}in".format(D), (int(mX), int(mY - 10)),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.55, color, 2)

        # show the output image
        cv2.imshow("Image", orig)
        cv2.waitKey(0)
