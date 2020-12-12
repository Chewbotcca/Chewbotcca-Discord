# Chewbotcca Contributing Guidelines

Thank you for wanting to contribute to Chewbotcca! Your efforts are greatly appreciated.

## Deciding What to Contribute

Hardest part about contributing is deciding what to add. 
There are plenty of issues for you to handle if you so choose.
Any labelled "Good First Issue" are relatively simple and easy to handle, give them a shot and see!

If you're concerned about adding something we may not want to add, feel free to hop onto [#chewbotcca on Chew's Discord Server](https://discord.gg/UjxQ3Bh) and ask there!

## Installing/Setting Up Enviornemnt

If you're in the Codespaces beta, all you need to do is fork this repository, click Clone, then click "Open with Codespaces."
This will set up a fully working environment for you to use! Simply do all your work here and finish the flow when done.

Otherwise, you need to set up the long, tedious way.

### Requirements

* Java 15
* Maven
* Git

1) Fork this repository
2) `git clone (your fork repo)`
3) `git checkout (change summary)` (such as `git checkout command/rory`) working on your default branch is anti-pattern, use branches.
4) Do all your changes.
5) Run `mvn clean package` to make sure it builds properly.

Once done, `git add` your files, add a commit messages, push, then PR!

## Opening Pull Request

Once your PR is open, I will review it if necessary, and test to ensure it works properly as well. If all is well, I will merge it in!
