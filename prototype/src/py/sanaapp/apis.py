from sanaapp import app
from core import utils

from flask import jsonify, request

@app.route("/request/checksums", methods=["POST"])
def return_checksums():
    response_payload = {}

    file_name = request.form.get("name")
    file_size = request.form.get("size", type = int)
    if file_name is None or file_size is None:
        return _get_error_payload("Invalid arguments", 400)

    app.logger.info("Getting checksums for {} with size {}".format(file_name, file_size))
    
    rolling, md5 = utils.get_checksums(file_name, file_size)
    response_payload["rolling"] = rolling
    response_payload["md5"] = md5
    return jsonify(**response_payload), 200

@app.route("/upload/instructions", methods=["POST"])
def receive_instruction():
    response_payload = {}

    # Extract binary data from (application/octet-stream)
    chunk = request.files["chunk"]
    index = request.form.get("index")
    file_name = request.form.get("name")
    if index is None or chunk is None or file_name is None:
        return _get_error_payload("Invalid arguments", 400)

    app.logger.info(index)
    app.logger.info(chunk)

    try:
        int_index = int(index)
        # chunk.read() returns the content of a file as binary
        utils.build_file(int_index, chunk.read(), file_name)
    except ValueError:
        return _get_error_payload("Invalid arguments", 400)

    return jsonify(**response_payload), 200

def _get_error_payload(payload, error_message, error_code):
    payload["error"] = error_message
    return jsonify(**payload), error_code
