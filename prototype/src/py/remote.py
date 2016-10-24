import os

from constants import *
from utils import *

def main():
    build_partial_file(IMAGE_PATH,
                       'in/partial_1_rolling.sum',
                       'in/partial_1_md5.sum',
                       'out/partial_1.jpeg')
    results = build_wrapper(local_filename='out/partial_1.jpeg')
    print 'Built file of size: %d bytes' % results

main()
