from sanaapp import app
from core import utils

from flask import jsonify, request

@app.route("/upload/instructions", methods=["POST"])
def receive_instruction():
    response_payload = {}

    # Extract binary data from (application/octet-stream)
    chunk = request.files["chunk"]
    index = request.form.get("index", None)
    if index is None or chunk is None:
        return _get_error_payload("Invalid arguments", 400)

    app.logger.info(index)
    app.logger.info(chunk)

    try:
        int_index = int(index)
        # chunk.read() returns the content of a file as binary
        utils.build_file(int_index, chunk.read())
    except ValueError:
        return _get_error_payload("Invalid arguments", 400)

    return jsonify(**response_payload), 200

def _get_error_payload(payload, error_message, error_code):
    payload["error"] = error_message
    return jsonify(**payload), error_code
