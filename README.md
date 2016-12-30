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

To run python code or python server:
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
