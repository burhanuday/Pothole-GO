# Python program to Edge detection
# using OpenCV in Python
# using Sobel edge detection
# and laplacian method
import cv2
import numpy as np
from matplotlib import pyplot as plt

img = cv2.imread('1_test_pothole.jpg', 0)
edges = cv2.Sobel(img, cv2.CV_64F, 1, 0, ksize=5)

#hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)

# Calcution of Sobelx
sobelx = cv2.Sobel(img, cv2.CV_64F, 1, 0, ksize=5)

# Calculation of Sobely
sobely = cv2.Sobel(img, cv2.CV_64F, 0, 1, ksize=5)

# Calculation of Laplacian
laplacian = cv2.Laplacian(img, cv2.CV_64F)

cv2.imshow('original img', img)
cv2.imshow('sobelx', sobelx)
cv2.imshow('sobely', sobely)
cv2.imshow('laplacian', laplacian)
cv2.imshow('edges', edges)

cv2.waitKey(0)