import sys

import utils


# This assumes that *_rolling.sum and *_md5.sum already exists.
def main():
	test_filename = sys.argv[1]
	original_file_path = sys.argv[2]

	print "| Building for {0}\n".format(test_filename)
	utils.build_partial_file(
		original_file_path,
		"../../java/src/test/java/rsync/client/uploader/assets/{0}_rolling.sum".format(test_filename),
		"../../java/src/test/java/rsync/client/uploader/assets/{0}_md5.sum".format(test_filename),
		"../out/{0}.jpeg".format(test_filename)
	)


main()