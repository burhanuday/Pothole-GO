import cv2

import numpy as np
from matplotlib import pyplot as plt

im = cv2.imread('2.png')

# CONVERT TO GRAYSCALE

gray1 = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
# save image

cv2.imwrite('graypothholeresult.jpg', gray1)

# CONTOUR DETECTION CODE
imgray = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
ret, thresh = cv2.threshold(imgray, 127, 255, 0)
im, contours1, _ = cv2.findContours(thresh, cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)
im, contours2, _ = cv2.findContours(thresh, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

img1 = im.copy()
img2 = im.copy()

# out = cv2.drawContours(img1, contours1, -1, (255, 0, 0), 2)
# cv2.imshow('out', out)

out = cv2.drawContours(img2, contours2, -1, (250, 250, 250), 1)

# out = np.hstack([img2, img2])

cv2.imshow('img2', img2)
cv2.waitKey(0)
plt.subplot(331), plt.imshow(im), plt.title('GRAY')
plt.xticks([]), plt.yticks([])

# im = cv2.imread('15.png', 0)

kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (2, 2))

# Fill any small holes
closing = cv2.morphologyEx(im, cv2.MORPH_CLOSE, kernel)
# Remove noise
opening = cv2.morphologyEx(closing, cv2.MORPH_OPEN, kernel)

# Dilate to merge adjacent blobs
dilation = cv2.dilate(opening, kernel, iterations=9)

# threshold
th = dilation[dilation < 240] = 0

ret, thresh = cv2.threshold(im, 127, 255, 0)
_, contours, hierarchy = cv2.findContours(thresh, 1, 2)
cnt = contours[0]
M = cv2.moments(cnt)
print(M)
perimeter = cv2.arcLength(cnt, True)
print(perimeter)
area = cv2.contourArea(cnt)
print(area)
epsilon = 0.1 * cv2.arcLength(cnt, True)
approx = cv2.approxPolyDP(cnt, epsilon, True)
print(epsilon)
print(approx)
count = 0
for c in contours:
    rect = cv2.boundingRect(c)
    if rect[2] < 100 or rect[3] < 100: continue

    print(cv2.contourArea(c))
    x, y, w, h = rect
    cv2.rectangle(img2, (x, y), (x + w, y + h), (0, 255, 0), 8)
    cv2.putText(img2, 'Moth Detected', (x + w + 40, y + h), 0, 2.0, (0, 255, 0))
cv2.imshow("Show", im)
cv2.waitKey(0)

cv2.destroyAllWindows()

# Setup SimpleBlobDetector parameters.
params = cv2.SimpleBlobDetector_Params()

# filter by color
params.filterByColor = True
params.blobColor = 0

# Filter by Convexity

params.filterByConvexity = True
params.minConvexity = 0.85

# Filter by Inertia

params.filterByInertia = True
params.minInertiaRatio = 0.08

params.filterByCircularity = True
params.minCircularity = 0.4

# Create a detector with the parameters
ver = (cv2.__version__).split('.')
print(ver)
if int(ver[0]) < 3:
    detector = cv2.SimpleBlobDetector(params)
else:
    detector = cv2.SimpleBlobDetector_create(params)

# Detect blobs.
keypoints = detector.detect(im)

# Draw detected blobs as red circles.
# cv2.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS ensures the size of the circle corresponds to the size of blob
im_with_keypoints = cv2.drawKeypoints(im, keypoints, np.array([]), (0, 0, 255),
                                      cv2.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS)

# Show keypoints
cv2.imwrite("keypoints.jpg", im_with_keypoints)
x = cv2.imread("keypoints.jpg", 1)
cv2.imshow('keypoints', x)
print("Total number of potholes")
print(len(keypoints))
