import struct
import hashlib
import os

BLOCK_SIZE = 1024 # Bytes

def _adler_32(block):
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


def _md5(block):
    m = hashlib.md5(block)
    return m.hexdigest()


def get_hash(file_dir, filename):
    image_path = os.path.join(file_dir, filename)

    if not os.path.isfile(image_path):
        return (None, None, 0, 0)

    rolling_wf = []
    md5_wf = []
    lastBlockSize = 0
    with open(image_path, 'rb') as f:

        byte = f.read(BLOCK_SIZE)
        while byte != "":

            readByteSize = len(byte)
            if 0 < readByteSize and readByteSize < BLOCK_SIZE:
                lastBlockSize = readByteSize

            rolling_wf.append(_adler_32(byte))
            md5_wf.append(_md5(byte))

            byte = f.read(BLOCK_SIZE)

    return (rolling_wf, md5_wf, BLOCK_SIZE, readByteSize)
