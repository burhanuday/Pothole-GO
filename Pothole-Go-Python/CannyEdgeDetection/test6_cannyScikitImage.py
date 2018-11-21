import numpy as np
import matplotlib.pyplot as plt
from scipy import ndimage as ndi
from skimage import feature
from skimage import io
from skimage.color import rgb2gray
import cv2

im = rgb2gray(io.imread('rsz_pothole1.jpg'))


# edges = feature.canny(im)
# io.imshow(edges)
# io.show()

# Generate noisy image of a square
# im = np.zeros((128, 128))
# im[32:-32, 32:-32] = 1

# im = ndi.rotate(im, 15, mode='constant')
# im = ndi.gaussian_filter(im, 4)
# im += 0.2 * np.random.random(im.shape)


# Compute the Canny filter for two values of sigma
edges1 = feature.canny(im, sigma=4)
edges2 = feature.canny(im, sigma=2.95)

# display results
fig, (ax1, ax2, ax3) = plt.subplots(nrows=1, ncols=3, figsize=(8, 3), sharex=True, sharey=True)

ax1.imshow(im, cmap=plt.cm.gray)
ax1.axis('off')
ax1.set_title('noisy image', fontsize=20)

ax2.imshow(edges1, cmap=plt.cm.gray)
ax2.axis('off')
ax2.set_title('Canny filter, $\sigma=4$', fontsize=20)

ax3.imshow(edges2, cmap=plt.cm.gray)
ax3.axis('off')
ax3.set_title('Canny filter, $\sigma=2.95$', fontsize=20)

fig.tight_layout()

plt.savefig('test6_canny_4.jpg', dpi=None, facecolor='w', edgecolor='w',
            orientation='portrait', papertype=None, format=None,
            transparent=False, bbox_inches=None, pad_inches=0.1,
            frameon=None)

plt.show()

