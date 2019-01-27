from PIL import Image
import os, sys

path = "../test_ML/testImages_pothole/NoPothole/"
dirs = os.listdir(path)


def resize():
    for item in dirs:
        if os.path.isfile(path + item):
            im = Image.open(path + item)
            f, e = os.path.splitext(path + item)
            imResize = im.resize((512, 512), Image.ANTIALIAS)
            imResize.save(f + ' resized.jpg', 'JPEG', quality=90)


resize()
