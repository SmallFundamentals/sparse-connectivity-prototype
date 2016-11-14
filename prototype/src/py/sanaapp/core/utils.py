from sanaapp import app
import os

BLOCK_SIZE = 1024

# TODO: This assumes that we already have a file that is partially built
# (out/output_http.jpeg).
def build_file(index, chunk_data, local_filename="out/output_http.jpeg"):
    """
    Write data to the file based on the index of given data.
    """
    local_copy = _get_blocks(local_filename)
    app.logger.info("# of local blocks: {}".format(len(local_copy)))

    if index < len(local_copy):
        _add_chunk(index, chunk_data, local_copy, local_filename)
    else:
        _append_chunk(chunk_data, local_filename)

def _get_blocks(img_path):
    """
    Given an image path, read its contents and break them down
    into blocks.

    Returns:
    An in-memory representation of the file.
    """
    in_mem_copy = []
    with open(img_path, 'rb') as f:
        byte = f.read(BLOCK_SIZE)
        while byte != "":
            in_mem_copy.append(byte)
            byte = f.read(BLOCK_SIZE)
    return in_mem_copy

def _add_chunk(index, chunk_data, blocks, local_filename):
    """
    Write data inside the local file.

    It copies data from the local file and also adds given data
    to the appropriate place.

    For example, suppose we have 3 blocks in the local file, [b0, b1, b2],
    Suppose we receive a new data block with index 1, d1. A new local file
    will be [b0, d1, b1, b2]. This means that the index of new data
    represents the correct place of a given block in a file.
    """
    temp_output_filename = local_filename + ".temp"

    with open(temp_output_filename, "w") as output_file:
        for i, each_block in enumerate(blocks):
            if i == index:
                output_file.write(chunk_data)
            output_file.write(each_block)

    # Remove the local file and change the temp file to a new
    # local file.
    os.remove(local_filename)
    os.rename(temp_output_filename, local_filename)

def _append_chunk(chunk_data, file_name):
    """
    Append data to the local file
    """
    with open(file_name, "a+") as output_file:
        output_file.write(chunk_data)
