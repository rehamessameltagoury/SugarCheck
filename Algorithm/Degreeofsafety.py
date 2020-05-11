import cv2
import pytesseract
pytesseract.pytesseract.tesseract_cmd = 'C:/Program Files/Tesseract-OCR/tesseract.exe'
import os
import re
from difflib import SequenceMatcher
from PIL import Image

# get grayscale image
def get_grayscale(image):
    return cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

# apply adaptive thresholding
def thresholding(image):
    return cv2.adaptiveThreshold(image, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 115, 1)

# Preprocessing the image to improve the OCR accuracy
def preprocessing(image):
    gray =  get_grayscale(image)
    return thresholding(gray)

# apply regular expressions to get the words making up the ingredients
def cleaning(text):
	text= re.sub('[*|\n|\|(|)|.]',' ',text)
	text = re.sub('[}|{|\|/|,|:]',' ',text)
	text = re.sub(' +',' ',text)
	ingredients = text.split()
	return ingredients


if __name__ == '__main__':

            # Reading an image of ingredients label
            img = cv2.imread('C:\\Users\\win 7\\Desktop\\Image.png')

            # Preprocessing the image by converting it to gray scale and applying adaptive thresholding to improve OCR accuracy
            preprocessed_image = preprocessing(img)

            # Save the preprocessed image to send it to the OCR
            cv2.imwrite("new.png", preprocessed_image)

            # Apply OCR
            text = pytesseract.image_to_string(Image.open(os.path.abspath('new.png')))

            # Applying Regular expression to get the words making up the text and saving it as a list of ingredients
            ingredients = cleaning(text)

            # Converting all words to be lowercase to be able to compare it to the sugars list which contains all word in lowercase
            ingredients = [x.lower() for x in ingredients]

            # The list of sugars that we will search for in the ingredients to warn the user if any of them is found
            sugars = ['sugar', 'glucose', 'fructose', 'dextrose', 'sucrose', 'syrup', 'hydrogenated', 'lard', 'molasses', 'maltose', 'lactose']

            # Counter to count the number of sugars present in the ingredients if any
            weary_ingredients = 0

            # Searching if any of the sugars is found in the ingredients
            for i in range(len(sugars)):
                for j in range(len(ingredients)):

                    # Due to the tesseract not being 100% accurate, we get ratio of similarity if it's greater than 70% threshold
                    # than the two words are most probably the same
                    if SequenceMatcher(None, sugars[i], ingredients[j]).ratio() >= 0.7:
                        weary_ingredients = weary_ingredients + 1

            if weary_ingredients > 0:
                print("NOT SAFE")
            else:
                print("SAFE")


