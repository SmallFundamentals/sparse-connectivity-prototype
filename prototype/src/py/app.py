from flask import Flask

app = Flask(__name__)


@app.route('/')
def index():
    return "Hello from Sparse Connectivity Prototype"


if __name__ == '__main__':
    app.run(debug=True)
