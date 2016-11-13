from sanaapp import app

from flask import jsonify, request

@app.route("/upload/instructions", methods=["POST"])
def receive_instruction():
    response_payload = {}

    # Extract binary data from (application/octet-stream)
    chunk = request.files["chunk"]
    index = request.form.get("index", None)
    if index is None or chunk is None:
        response_payload["error"] = "Invalid arguments"
        return jsonify(**response_payload), 400

    app.logger.info(index)
    app.logger.info(chunk)
    # chunk.read() returns the content of a file as binary

    return jsonify(**response_payload), 200
