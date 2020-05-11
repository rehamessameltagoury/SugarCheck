import os.path
import numpy as np
import cv2
import json
from flask import Flask,request,Response
import uuid
import pytesseract
pytesseract.pytesseract.tesseract_cmd = 'C:/Program Files/Tesseract-OCR/tesseract.exe'
from pytesseract import Output
import os
import re
from difflib import SequenceMatcher
from PIL import Image

# get grayscale image
def get_grayscale(image):
    return cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)


# thresholding
def thresholding(image):
    return cv2.threshold(image, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)[1]



def cleaning(text):
	text= re.sub('[*|\n|\|(|)|.]',' ',text)
	text = re.sub('[}|{|\|/|,|:]',' ',text)
	text = re.sub(' +',' ',text)
	ingredients = text.split()
	return ingredients

def sugarcheck(img,file):

				gray = get_grayscale(img)
				thresh = thresholding(gray)
				text = pytesseract.image_to_string(Image.open(file))
				ingredients = cleaning(text)
				sugars=['glucose','fructose','dextrose']

				ingredients = [x.lower() for x in ingredients]
				weary_ingredients = 0
				for i in range(len(sugars)):
					for j in range(len(ingredients)):

						if SequenceMatcher(None,sugars[i], ingredients[j]).ratio() >= 0.7:
							 weary_ingredients = weary_ingredients + 1
				result=""
				if weary_ingredients > 0:
					result="NOT SAFE"
				else:
					result="SAFE"

				return json.dumps(result)


# API

app = Flask(__name__)

@app.route('/api/upload',methods=['POST'])


def upload():
    img = cv2.imdecode(np.fromstring(request.files['image'].read(),np.uint8),cv2.IMREAD_UNCHANGED)
    file_path=request.files['image']
    # process image
    img_processed =sugarcheck(img,file_path)
    # response
    return Response(response=img_processed,status=200,mimetype="application/json")


app.run(host="0.0.0.0",port=5000)






