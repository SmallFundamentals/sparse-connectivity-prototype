JFLAGS = -d .
JC = javac
.SUFFIXES: .java .class

CLASSES = \
	Adler32.java \
	
MAIN = \
	ClientUploader.java

all:
	$(JC) $(JFLAGS) $(CLASSES)
	$(JC) $(JFLAGS) $(MAIN)
	java -cp . client_uploader.ClientUploader

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class *.sum
