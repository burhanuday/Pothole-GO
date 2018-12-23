from scipy import misc
from skimage import io
from skimage import color
from skimage import measure
import matplotlib.pyplot as plt
from skimage.draw import ellipse
from skimage.measure import find_contours, approximate_polygon, subdivide_polygon
import cv2

img = io.imread('rsz_pothole1.jpg')  # read image
gray_img = color.colorconv.rgb2gray(img)  # convert to grayscale

contours = measure.find_contours(gray_img, 0.25)
for n, contour in enumerate(contours):
    plt.plot(contour[:, 1], contour[:, 0], linewidth=2)

contour = contours[0]
new_s = contour.copy()
appr_s = approximate_polygon(new_s, tolerance=0.7)

fig, (ax1, ax2) = plt.subplots(ncols=2, figsize=(8, 8))
ax2.plot(contour[:, 0], contour[:, 1])
ax1.plot(appr_s[:, 0], appr_s[:, 1])
plt.show()
cv2.imshow('orginal', img)
cv2.waitKey(0)
