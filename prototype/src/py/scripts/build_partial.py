import sys

from constants import IMAGE_PATH
import utils


# This assumes that *_rolling.sum and *_md5.sum already exists.
def main():
	for i in xrange(1, len(sys.argv)):
		test_filename = sys.argv[i]
		print "| Building for {0}\n".format(test_filename)
		utils.build_partial_file(
			IMAGE_PATH,
			"../../java/src/test/java/rsync/client/uploader/assets/{0}_rolling.sum".format(test_filename),
			"../../java/src/test/java/rsync/client/uploader/assets/{0}_md5.sum".format(test_filename),
			"../out/{0}.jpeg".format(test_filename)
		)
		print "\n"

main()