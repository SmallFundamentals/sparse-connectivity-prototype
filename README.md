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

Before you push your commit:
```shell
git fetch
git rebase -i origin/master
<fix any conflicts>
git push
```
