import struct
import hashlib
import os

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


def main():
    BLOCK_SIZE = 1024 #Bytes
    IMAGE_PATH = os.path.join(os.path.dirname(__file__), '../../assets/sm_img.jpeg')

    local_copy = []

    with open(IMAGE_PATH, 'rb') as f:
        rolling_wf = open('py_rolling.sum','w')
        md5_wf = open('py_md5.sum','w')

        byte = f.read(BLOCK_SIZE)
        while byte != "":
            local_copy.append(byte)
            rolling_checksum = adler_32(byte)
            md5_checksum = md5(byte)

            rolling_wf.write(str(rolling_checksum) + "\n")
            md5_wf.write(md5_checksum + "\n")

            byte = f.read(BLOCK_SIZE)

    output_wf = open('built.jpeg','w')
    size = 0
    size_local = 0
    size_foreign = 0
    with open('instr.out') as f:
        content = f.readlines()
        """
        # Code that uses original image.
        for line in local_copy:
            output_wf.write(line)
            size += len(line)
        # NOTE TO SELF: This proves that the instructions generated is likely at fault. Writing strictly from local data works as expected.
        # Next step: Fix Maven dependencies, then generate instruction set that includes ALL data, and see what happens.
        """
        for line in content:
            data = line
            if line.strip().isdigit():
                data = local_copy[int(line.strip())]
                size_local += len(data)
            else:
                size_foreign += len(data)
            output_wf.write(data)
            size += len(data)
    print size
    print "LOCAL " + str(size_local)
    print "FOREIGN " + str(size_foreign)
main()
