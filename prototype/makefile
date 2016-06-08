PY_DIR = src/py
JAVA_DIR = src/java

all:
	make py
	make java
	
py:
	@echo Making Python...
	cd $(PY_DIR); make

java:
	@echo Making Java...
	cd $(JAVA_DIR); make

clean:
	cd $(PY_DIR); make clean
	cd $(JAVA_DIR); make clean
	$(RM) *.sum
