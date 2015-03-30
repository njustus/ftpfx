# ftpfx - the new ftp client
ftpfx is a new Ftp client **totally written in scala** using **javafx** as new, beautiful gui.
It's a new approach with the idea that you need only **one client** for **all** your **systems** your using. 
(Java/Scala => Write once, use everywhere)

It all started as a project for learning scala in the university.

At the moment it's quite "java-heavy" because of all the stream, socket and javafx classes 
that it's using but i didn't find any good alternative for scala.

# Requirements
In order to use ftpfx you need several important sdk's:
 1. [Scala SDK](http://www.scala-lang.org/download/) >= 2.11.5
 2. [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) >= 8_40

(You need JDK >= 8_40 because standard-dialogues for JavaFX aren't supported below 8_40.)

# Development
In order to contribute to ftpfx you need to follow these steps:

1. Get the [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) >= 8_40
2. Get the [Scala SDK](http://www.scala-lang.org/download/) >= 2.11.5
3. Get an IDE that can handle Scala:
  - [Scala-IDE](http://scala-ide.org/)
  - [Eclipse](https://eclipse.org/) with Scala-Plugins

4. Ensure scala and java works by typing the following into a terminal:
  - java -version
  - javac -version
  - scala -version
  - scalac -version

5. Open up your IDE, if it's Scala-IDE or Eclipse do the following:
  1. Open up File - Import - Git - **Projects from Git**
  2. Select "Clone URI"
  3. Setup the upstreams as follows:
   - URI: https://github.com/nicothm/ftpfx.git
   - hit next (don't enter any authentication)
   - Select the **branches** you want to clone, usually the master
   - Select a **destination** for the new local-git-repository and hit next
   - Select "import existing projects" and hit next
   - Ensure that the project "FtpClient" is listed and ticked, then hit finish

Now the project is imported into ScalaIDE/Eclipse. The only thing that's left is to **edit the build path**. So do that:

1. Right click the project
2. Select Build Path - **Edit Build Path**
3. Add the following **librarys**:
  - Java JDK8_40 (should already be added by the ide)
  - Scala SDK 2.5.11
  - JUnit 4 (only if you like to run the tests)
4. Inside the "Edit Build Path" window go to the **Source** tab and add the following folders:
  - FtpClient/**rsc**
  - FtpClient/**src**
  - FtpClient/**tests**
5. Wait until the ide has built the workspace/project
6. **Open** the main-gui-class in **"src/ftp/ui/FtpGui.scala"**
7. Run this class as **"Scala Application"**

That's it. If the GUI appears the project and ftpfx are working. Now you can start developing cool new features.

And don't forget to setup a pull request after you finished your work. ;)
