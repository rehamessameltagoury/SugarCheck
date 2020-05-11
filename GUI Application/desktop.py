from PyQt5 import QtGui
from PyQt5.QtWidgets import QApplication, QWidget, QVBoxLayout, QPushButton, QFileDialog, QLabel, QTextEdit
import sys

from PyQt5.QtGui import QPixmap
import re
import cv2
import numpy as np
import os
from difflib import SequenceMatcher
from PIL import Image
import pytesseract
pytesseract.pytesseract.tesseract_cmd = 'D:/Program Files/Tesseract-OCR/tesseract.exe'


class Window(QWidget):
    def __init__(self):
        super().__init__()

        self.title = "Sugar Check"
        self.top = 200
        self.left = 500
        self.width = 400
        self.height = 300
        self.imagePath = '1'
        self.InitWindow()

    def InitWindow(self):
        self.setWindowIcon(QtGui.QIcon("icon.png"))
        self.setWindowTitle(self.title)
        self.setGeometry(self.left, self.top, self.width, self.height)

        vbox = QVBoxLayout()


        self.btn1 = QPushButton("Open Image")
        self.btn1.clicked.connect(self.getImage)
        self.resize(400, 300)

        vbox.addWidget(self.btn1)

        self.label = QLabel(" ")
        vbox.addWidget(self.label)
        self.btn2 = QPushButton("Check Sugar!")
        vbox.addWidget(self.btn2)
        self.btn2.hide()
        self.label2 = QLabel(" ")
        vbox.addWidget(self.label2)

        self.setLayout(vbox)

        self.show()

    def checkImage(self):
        self.label2.show()
        img = cv2.imread(self.imagePath)
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        thresh = cv2.adaptiveThreshold(gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 115, 1)
        cv2.imwrite('new.png', thresh)
        text = pytesseract.image_to_string(Image.open(os.path.abspath('new.png')))
        text = re.sub('[*|\n|\|(|)|.]', ' ', text)
        text = re.sub('[}|{|\|/|,|:]', ' ', text)
        text = re.sub(' +', ' ', text)
        ingredients = text.split()
        sugars = ['glucose', 'fructose', 'dextrose', 'molasses', 'sucrose', 'lactose', 'maltose', 'syrup', 'lard', 'hydrogenated', 'sugar']
        ingredients = [x.lower() for x in ingredients]
        weary_ingredients = 0
        for i in range(len(sugars)):
            for j in range(len(ingredients)):

                if SequenceMatcher(None, sugars[i], ingredients[j]).ratio() >= 0.7:
                    weary_ingredients = weary_ingredients + 1

        if weary_ingredients > 0:
            self.label2.setText("NOT SAFE")
        else:
            self.label2.setText("SAFE")




    def getImage(self):
        self.label2.hide()
        fname = QFileDialog.getOpenFileName(self, 'Open file',
                                            'c:\\', "Image files (*.jpg *.png *.webp)")
        if(fname != ('','')):
            self.imagePath = fname[0]
            pixmap = QPixmap(self.imagePath)
            self.label.setPixmap(QPixmap(pixmap))
            self.resize(pixmap.width(), pixmap.height())

            self.btn2.show()
            self.btn2.clicked.connect(self.checkImage)




App = QApplication(sys.argv)
window = Window()
sys.exit(App.exec())
