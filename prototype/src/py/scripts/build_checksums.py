import sys

from constants import IMAGE_PATH
import utils


# This builds the checksum for the given file.
def main():
	for i in xrange(1, len(sys.argv)):
		filepath = sys.argv[i]
		print "| Building for {0}\n".format(filepath)
		utils.calc(filepath)
		print "\n"

main()