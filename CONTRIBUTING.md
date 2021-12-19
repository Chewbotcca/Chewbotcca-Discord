# Chewbotcca Contributing Guidelines

Thank you for wanting to contribute to Chewbotcca! Your efforts are much appreciated.

## Deciding What to Contribute

The hardest part about contributing is deciding what to add.
There are plenty of issues for you to handle if you so choose.
Any labeled "Good First Issue" is relatively simple and easy to handle. Give them a shot and see!

If you're concerned about adding something we may not want to add, feel free to hop onto [#chewbotcca on the Rory Fanclub](https://discord.gg/UjxQ3Bh) and ask there!

## Installing/Setting Up Environment

~~If you're in the Codespaces beta, all you need to do is fork this repository, click Clone, then click "Open with Codespaces."
Clicking it will set up a fully working environment for you to use! Do all your work here and finish the flow when done.~~

Codespaces is currently on Java 15, while this project requires Java 16. You'll have to take the long, tedious way described below.

Otherwise, you need to set up via the long, tedious way.

### Requirements

* Java 16
* Maven (at least 3.6.3)
* Git

1) Fork this repository
2) `git clone (your fork repo link) Chewbotcca-Discord` then `cd Chewbotcca-Discord`
3) Run `git branch (change summary)` (such as `git branch command/rory`) then `git checkout (change summary)` working on your default branch is anti-pattern, so use branches.
4) Do all your changes.
5) Run `mvn clean package` to make sure it builds appropriately.

Once done, `git add` your files, add a commit message, push, then PR!

## Opening Pull Request

Once your PR is open, I will review it if necessary and test it to ensure it works properly. If all is well, I will merge it in!
