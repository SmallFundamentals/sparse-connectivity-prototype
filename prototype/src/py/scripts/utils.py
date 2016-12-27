import hashlib
import struct

from constants import *

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

# Rsync analyser helpers

def build_wrapper(instr_filename='in/instr.out',
                  local_filename=IMAGE_PATH,
                  output_filename='out/output.jpeg'):
    instructions = []
    with open(instr_filename) as f:
        content = f.readlines()
        for line in content:
            data = line
            if line.strip().isdigit():
                idx = int(line.strip())
                instructions.append(idx)
            else:
                instructions.append(data)
    return build(instructions, local_filename, output_filename)

def build(instructions,
          local_filename=IMAGE_PATH,
          output_filename='out/output.jpeg'):
    """
    Build a file given a set of instructions.

    The instructions are assumed to be in the following format
    """
    size = 0
    local_copy = get_blocks(local_filename)
    output_wf = open(output_filename,'w')
    for line in instructions:
        data = line
        if isinstance(line, (int, long)):
            data = local_copy[line]
        output_wf.write(data)
        size += len(data)
    return size

def calc(img_path,
         rolling_chksum_filename="out/py_rolling.sum",
         md5_chksum_filename="out/py_md5.sum"):
    """
    Given an image path, calculate the checksums for each block
    and write them to file.

    Returns:
    An in-memory representation of the file.
    """
    in_mem_copy = []
    with open(img_path, 'rb') as f:
        rolling_wf = open(rolling_chksum_filename,'w')
        md5_wf = open(md5_chksum_filename,'w')

        byte = f.read(BLOCK_SIZE)
        while byte != "":
            in_mem_copy.append(byte)
            # Calculate checksums
            rolling_checksum = adler_32(byte)
            md5_checksum = md5(byte)
            # Write checksums to file
            rolling_wf.write(str(rolling_checksum) + "\n")
            md5_wf.write(md5_checksum + "\n")
            # Read next byte
            byte = f.read(BLOCK_SIZE)
    return in_mem_copy

def get_blocks(img_path):
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

def build_partial_file(img_path,
                       rolling_chksum_filename,
                       md5_chksum_filename,
                       output_filename='out/partial.jpeg'):
    in_mem_copy = []
    rolling_checksums = [int(chksum) for chksum in
        open(rolling_chksum_filename, 'rb').read().splitlines()]
    md5_checksums = open(md5_chksum_filename, 'rb').read().splitlines()
    print "Rolling: {}".format(rolling_checksums)
    print "MD5: {}".format(md5_checksums)

    output_wf = open(output_filename,'w')

    with open(img_path, 'rb') as f:
        byte = f.read(BLOCK_SIZE)
        while byte != "":
            # Calculate checksums
            rolling_checksum = adler_32(byte)
            md5_checksum = md5(byte)
            # Checksums should match - either both contains, or both does not contain
            assert not ((rolling_checksum in rolling_checksums) ^ (md5_checksum in md5_checksums))
            if (rolling_checksum in rolling_checksums) and (md5_checksum in md5_checksums):
                output_wf.write(byte)
                in_mem_copy.append(byte)
            else:
                zero_bytes = bytearray(len(byte))
                output_wf.write(zero_bytes)
            # Read next byte
            byte = f.read(BLOCK_SIZE)

    output_wf.close()
    return in_mem_copy
