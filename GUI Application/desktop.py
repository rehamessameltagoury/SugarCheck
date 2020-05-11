import sys
from os import path
import re
import cv2
import numpy as np
import os
from difflib import SequenceMatcher
from PIL import Image
from PyQt5 import QtCore, QtWidgets, QtGui
from PyQt5.QtGui import QIcon,QPixmap
from PyQt5.QtCore import pyqtSlot
from PyQt5.QtWidgets import QApplication, QLabel, QPushButton, QVBoxLayout, QWidget,  QFileDialog, QTextEdit
from PyQt5.QtGui import QPixmap
import pytesseract
pytesseract.pytesseract.tesseract_cmd = 'C:/Program Files/Tesseract-OCR/tesseract.exe'

class App(QWidget):

    def __init__(self):
        super().__init__()
        self.title = 'SugarCheck Application'
        self.left = 30
        self.top = 40
        self.width = 450
        self.height = 450
        self.initUI()

    def initUI(self):
        self.setWindowTitle(self.title)
        self.setGeometry(self.left, self.top, self.width, self.height)

        button = QPushButton('Choose an image', self)
        button.setToolTip('This is an example button')
        button.move(150, 100)
        button.clicked.connect(self.on_click)

        self.label = QLabel(self)
        self.label.setText('Result:               ')
        self.label.move(170,150)
        self.show()

    @pyqtSlot()
    def on_click(self):
        fname = QFileDialog.getOpenFileName(self, 'Open file','c:\\', "Image files (*.jpg *.gif)")
        imagePath = fname[0]
        if imagePath !="":
            img = cv2.imread(imagePath)
            gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
            thresh = cv2.adaptiveThreshold(gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 115, 1)
            cv2.imwrite('new.png',thresh)
            text = pytesseract.image_to_string(Image.open(os.path.abspath('new.png')))
            text = re.sub('[*|\n|\|(|)|.]', ' ', text)
            text = re.sub('[}|{|\|/|,|:]', ' ', text)
            text = re.sub(' +', ' ', text)
            ingredients = text.split()
            sugars = ['glucose', 'fructose', 'dextrose']
            ingredients = [x.lower() for x in ingredients]
            weary_ingredients = 0
            for i in range(len(sugars)):
                for j in range(len(ingredients)):

                    if SequenceMatcher(None, sugars[i], ingredients[j]).ratio() >= 0.7:
                        weary_ingredients = weary_ingredients + 1

            if weary_ingredients > 0:
                self.label.setText("NOT SAFE")
            else:
                self.label.setText("SAFE")

if __name__ == '__main__':
    app = QApplication(sys.argv)
    ex = App()
    sys.exit(app.exec_())
