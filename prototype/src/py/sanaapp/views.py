from sanaapp import app

@app.route("/")
def index():
    return "Hello from Sparse Connectivity Prototype"
