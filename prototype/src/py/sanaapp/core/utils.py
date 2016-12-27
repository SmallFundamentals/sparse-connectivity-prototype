import hashlib
import os
import struct

from sanaapp import app


BLOCK_SIZE = 1024

def get_checksums(file_name, file_size=None):
    """
    Get checksums of a given file. If a file doesn't exist, an empty file
    is created and it gets filled with zero blocks.
    """
    rolling_checksum = []
    md5_checksum = []

    file_path = "out/" + file_name
    if os.path.isfile(file_path):
        with open(file_path, "r") as f:
            byte = f.read(BLOCK_SIZE)
            while byte != "":
                rolling_checksum.append(adler_32(byte))
                md5_checksum.append(md5(byte))
                byte = f.read(BLOCK_SIZE)
    else:
        assert file_size is not None
        # Write zero-filled blocks
        num_blocks, remaining_size = divmod(file_size, BLOCK_SIZE)
        with open(file_path, "w") as f:
            for _ in xrange(num_blocks):
                zero_block = bytearray(BLOCK_SIZE)
                f.write(zero_block)

            if remaining_size > 0:
                last_block = bytearray(remaining_size)
                f.write(last_block)

    return (rolling_checksum, md5_checksum)


def build_file(index, chunk_data, file_name):
    """
    Write data to the file based on the index of given data.
    """
    local_file_name = "out/" + file_name
    app.logger.info("Writing data at index {} in {}".format(index, local_file_name))
    _write_block(index, chunk_data, local_file_name)
    app.logger.info("Wrote {} bytes of data".format(len(chunk_data)))


def _write_block(index, chunk_data, local_file_name):
    """
    Write data inside the local file at the given index.

    It assumes that the partial file has been correctly built
    and its missing data is represented as zero-filled blocks.
    """
    if not os.path.isfile(local_file_name):
        app.logger.info("{} doesn't exist".format(local_file_name))
        return

    with open(local_file_name, "r+b") as f:
        f.seek(index * BLOCK_SIZE)
        f.write(chunk_data)


# Core checksum algorithms

def adler_32(block):
    large_prime = 65521
    block_size = len(block)
    # https://docs.python.org/2/library/struct.html#format-characters
    data = struct.unpack("b" * block_size, block)

    a = 0
    b = 0
    for i in range(block_size):
        a += data[i]
        b += (block_size - i) * data[i]
    a = a % large_prime
    b = b % large_prime
    return a + (b * (2**16))


def md5(block):
    m = hashlib.md5(block)
    return m.hexdigest()
