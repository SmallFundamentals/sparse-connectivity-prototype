// Begin rolling checksum process
		int currentIndex = 0;
		byte[] b = new byte[BLOCK_SIZE];

		int bytesRead;
		while ((bytesRead = dataStream.read(b)) != -1) {
			for (int i = 0; i < bytesRead; i++) {
				int numRep = b[i];
				System.out.println(numRep);
				fos.write(String.valueOf(numRep));
			}
			long hash = adler32(b, bytesRead);
			// System.out.println(hash);
		}
		fos.close();