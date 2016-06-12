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

Install Apache Commons:
- Download binaries and extract jar file (e.g. commons-codec-1.10.jar)
- Move jar files to /Library/Java/Extensions/

Install virtualenv and packages:
- [Follow this](http://flask.pocoo.org/docs/0.11/installation/)
- `pip install -r requirements.txt` installs all packages required for running the server. This must be done while the environment is activated. 
- DO NOT run `pip install Flask`, this is done by the above command
- DO NOT push the folder `venv` created by the above command

To run:
```shell
make server (this runs the python code directly)
make client (this compiles all the Java files for the package and then runs it)

python app.py (this runs the Flask server)
```


Before you push your commit:
```shell
git fetch
git rebase -i origin/master
<fix any conflicts>
git push
```
