# sparse-connectivity-prototype
A prototype of our solution to SANA's sparse connectivity problem

**Rules**

As we should be trying to iterate very quickly, I'm not expecting us to fork, make PRs etc.

I would ask that we do the following however:

1. **Keep things clean**

  Folder names that make sense and divide stuff in a logical manner.
  
  If there are temporary files (e.g. compiled programs, generated files), do not commit them and instead add them to .gitignore (e.g. I added *checksum)
  
2. **Test before you push**

  We're not going to have a build process, and we're not doing PRs, so it'll be hard to track down stuff if they break, so let's try to keep them not broken.
  
  As a general rule of thumb, try to make things no more broken than before you pushed. 
  
  Regression is bad.
  
3. **Use common sense**

  Self-explanatory. Try not to do things that would get you yelled if you did it at work.
  
  
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
mvn exec:java -D exec.mainClass=rsync.client.uploader.Main -DskipTests

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
