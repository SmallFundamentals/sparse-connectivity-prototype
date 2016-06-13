from sanaapp import app

from core import file_reader
from flask import jsonify

import os

@app.route("/hash/<filename>", methods=["GET"])
def get_file_hash(filename):
    temp_dir = os.path.join(os.path.dirname(__file__), "../../../assets/")

    (rolling, md, default_size, last_size) = file_reader.get_hash(temp_dir, filename)
    if rolling is None and md is None:
        app.logger.warn("No such file: %s%s", temp_dir, filename)

    jsonDict = {
        "rolling": rolling,
        "md": md,
        "default_size": default_size,
        "last_size": last_size,
    }

    return jsonify(**jsonDict)
