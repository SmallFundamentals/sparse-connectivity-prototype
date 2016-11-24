from sanaapp import app
import os

BLOCK_SIZE = 1024

# TODO: This assumes that we already have a file that is partially built
# (out/copy_partial.jpeg).
def build_file(index, chunk_data, local_filename="out/copy_partial.jpeg"):
    """
    Write data to the file based on the index of given data.
    """
    app.logger.info("Writing data at index {} in {}".format(index, local_filename))
    _write_block(index, chunk_data, local_filename)

def _write_block(index, chunk_data, local_filename):
    """
    Write data inside the local file at the given index.

    It assumes that the partial file has been correctly built
    and its missing data is represented as zero-filled blocks.
    """
    with open(local_filename, "r+b") as f:
        f.seek(index * BLOCK_SIZE)
        f.write(chunk_data)
