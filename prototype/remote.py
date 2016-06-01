import struct
import hashlib


def adler_32(block):
    large_prime = 65521
    block_size = len(block)
    # https://docs.python.org/2/library/struct.html#format-characters
    data = struct.unpack("b" * block_size, block)

    a = 0
    b = 0
    for i in range(block_size):
        print data[i]
        a += data[i]
        b += (block_size - i) * data[i]

    a = a % large_prime
    b = b % large_prime
    return a + (b * 2**16)


def md5(block):
    m = hashlib.md5(block)
    return m.hexdigest()


def main():
    BLOCK_SIZE = 1024 #Bytes

    with open('img.jpeg', 'rb') as f:
        rolling_wf = open('rolling_checksum','w')
        md5_wf = open('md5_checksum','w')

        byte = f.read(BLOCK_SIZE)
        while byte != "":
            rolling_checksum = adler_32(byte)
            md5_checksum = md5(byte)

            rolling_wf.write(str(rolling_checksum) + "\n")
            md5_wf.write(md5_checksum+ "\n")

            byte = f.read(BLOCK_SIZE)


main()