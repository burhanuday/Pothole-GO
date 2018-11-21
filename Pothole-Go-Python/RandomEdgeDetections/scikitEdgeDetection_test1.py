from scipy import ndimage
import numpy as np
import cv2
from skimage import color
from skimage import io

gray_img = color.rgb2gray(io.imread('rsz_pothole1.jpg'))  #convert to grayscale

edge_horizontal = ndimage.sobel(gray_img, 0)
edge_vertical = ndimage.sobel(gray_img, 1)
magnitude = np.hypot(edge_horizontal, edge_vertical)

cv2.imshow('magnitude', magnitude)
cv2.waitKey(0)
