from flask import Flask

app = Flask(__name__)

import sanaapp.views
import sanaapp.apis
