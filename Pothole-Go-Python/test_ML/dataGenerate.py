import numpy as np
import matplotlib.pyplot as plt
import os
import cv2
from tqdm import tqdm
import random
import pickle

#images _ val

DATADIR = "E:/PythonProjects/pythonGo/Pothole-GO/Pothole-GO/Pothole-Go-Python/test_ML/testImages_pothole/"

CATEGORIES = ["Pothole", "NoPothole"]

training_data = []

IMG_SIZE = 250


def create_training_data():
    for category in CATEGORIES:  # do pothole and nopothole

        path = os.path.join(DATADIR, category)  # create path to pothole and nopothole
        class_num = CATEGORIES.index(category)  # get the classification  (0 or a 1). 0=pothole 1=nopothole

        for img in tqdm(os.listdir(path)):  # iterate over each image per pothole and no
            try:
                img_array = cv2.imread(os.path.join(path, img), cv2.IMREAD_GRAYSCALE)  # convert to array
                new_array = cv2.resize(img_array, (IMG_SIZE, IMG_SIZE))  # resize to normalize data size
                training_data.append([new_array, class_num])  # add this to our training_data
            except Exception as e:
                print("general exception", e, os.path.join(path, img))


create_training_data()

random.shuffle(training_data)

X = []
y = []

for features, label in training_data:
    X.append(features)
    y.append(label)

X = np.array(X).reshape(-1, IMG_SIZE, IMG_SIZE, 1)

pickle_out = open("X.pickle", "wb")
pickle.dump(X, pickle_out)
pickle_out.close()

pickle_out = open("y.pickle", "wb")
pickle.dump(y, pickle_out)
pickle_out.close()
