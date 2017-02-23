# sparse-connectivity-prototype
A prototype of our solution to SANA's sparse connectivity problem
  
**Watdo**

Setup your repo:
```shell
mkdir prototype
cd !$
git clone git@github.com:SmallFundamentals/sparse-connectivity-prototype.git .
```

[Run through this](https://help.github.com/articles/error-permission-denied-publickey/) if you get permission denied.

Install Maven:
- Run `brew install maven`
- Make sure you have Java 1.8 (or higher in the future)
- Read [docs](http://maven.apache.org/guides/) if you're not familiar with Maven

Install virtualenv and packages:
- [Follow this](http://flask.pocoo.org/docs/0.11/installation/)
- `pip install -r requirements.txt` installs all packages required for running the server. This must be done while the environment is activated. 
- DO NOT run `pip install Flask`, this is done by the above command
- DO NOT push the folder `venv` created by the above command

To run java code:
```shell
# Go to the folder that contains pom.xml
# pom.xml contains packages on which our code depends
cd prototype/src/java

# Install packages and build 
mvn install # with tests
mvn install -DskipTests # without tests

# Run
mvn exec:java -Dexec.mainClass=rsync.client.uploader.Main -DskipTests -Dexec.args="<test_filename>"

# Run test
mvn test
# Specific class
mvn test -Dtest=RsyncAnalyserTest

# Clean target and packages
mvn clean
```

To run python code or python server:**
```shell
make server (this runs the python code directly)

# Running Flask server
# Install pip and virtualenv
cd prototype/src/py
virtualenv venv
source venv/bin/activate
pip install -r requirements.txt
python runserver.py

# Cleaning up
deactivate # while venv is activated
rm -r venv/
```


Before you push your commit:
```shell
git fetch
git rebase -i origin/master
<fix any conflicts>
git push
```

**Testing Workflow**

For a new file, create a complete checksum first. Skip this step if you have a file's checksums already.
```
cd src/py/scripts
python build_checksums.py <path>
```
This script creates a copy of its complete md5 & rolling checksums in `py/out` and  `src/java/src/test/java/rsync/client/uploader/assets/`. Use this as your basis to create test cases.

Go to  `src/java/src/test/java/rsync/client/uploader/assets/` and create a copy of the complete checksums.
Take out one more or lines of the checksums, making sure you take out the same lines from both checksum files, and name it something like partial_x_rolling.sum and partial_x_md5.sum. This simulates incomplete uploads of files.
```
cd src/java/src/test/java/rsync/client/uploader/assets/
cp img_rolling.sum partial_x_rolling.sum
cp img_md5.sum partial_x_md5.sum
```

Using this, create zero-filled partial files to simulate our server's state. 
```
python build_partial.py <partial_x> <original file path>
```
The output will tell you the checksums received, and the building process. `zeroes` indicates a checksum missing, in which case a block of 0s will be used. An integer indicates that the block is matched. The output in general should conform to the test case that you created. e.g. if you delete the first three blocks, you should expect to see 3 `zeroes`, and then 3, 4, 5 etc. (since it is 0-indexed).

At this point, the test case has been built. (For missing data that doesn't cut-off at exactly a block cut-off point, we'll need another script to do that. For now, we can only build partial files based on checksums.)

To through a test scenario, start the python server and then run the Java code.
```
<See above for starting python server>
...
(In a different tab)
mvn exec:java -Dexec.mainClass=rsync.client.uploader.Main -DskipTests -Dexec.args="partial_x.jpeg <original file path>"
```

**Testing Done**

There are a few assumptions that have been made through testing:
```
- one way data transmission: from server to client only
- partial data chunk is either transmitted or not transmitted at all. There would not be a case where one data chunk is partially transmitted
- no case where server has more data than client
```

Here are the key areas that have been tested:

```
- single line partial sum deletion: beginning, end, in between
- multiple line partial sum deletion in random places of the .sum file
- empty file in server side (talked to Evan, where he said it would be handled by server)
- no deletion
```
