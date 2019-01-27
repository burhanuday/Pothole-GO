import cv2
import tensorflow as tf

CATEGORIES = ["Pothole", "NoPothole"]

img = cv2.imread('2resized_pothole.jpg', 1)
cv2.imshow('2resized_pothole', img)
cv2.waitKey(0)


def prepare(filepath):
    IMG_SIZE = 250
    img_array = cv2.imread(filepath, cv2.IMREAD_GRAYSCALE)
    print(img_array)
    new_array = cv2.resize(img_array, (IMG_SIZE, IMG_SIZE))
    print(new_array)
    return new_array.reshape(-1, IMG_SIZE, IMG_SIZE, 1)


model = tf.keras.models.load_model("64x3-CNN.model")

prediction = model.predict([prepare('2resized_pothole.jpg')])

print(prediction)
print(CATEGORIES[int(prediction[0][0])])














img = cv2.imread('2resized_pothole_1.jpg', 1)
cv2.imshow('2resized_pothole', img)
cv2.waitKey(0)
