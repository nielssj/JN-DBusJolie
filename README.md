D-Bus support for Jolie
============

**UNIX sockets**
The shared unix-java library (libunix-java.so) must be located in /usr/local/lib/jni. The library is included with [libmatthew](http://www.matthew.ath.cx/projects/java/).

***

**To simply work with the D-Bus extension from this repo:**
- Build using ant at source root `/jolie-src`
- Open the jolie NetBeans-project in `/jolie-src/jolie`
- In "Debug-configuration" 
  - Set working directory to: `/jolie-src/jolie/dist`
  - Add an argument for target jolie file to run e.g.: `../../playground/dbusSevice/server.ol`
  - Add the following argument to allow extensions to be loaded: `-l lib/*`

***

**To load additional jolie extensions or general java libraries:**
- Reference their .jar files in the jolie project (Right-click on "Libraries" and choose "Add JAR/Folder..")

NOTE: If the .jar files of extensions such as dbus are loaded correctly (from their own `/dist` folder) the NetBeans debugger will be able to use the source to aid debugging.

***

**To include the standard jolie language libraries (e.g. console.iol)**
- Add an argument: `-i ../../include`

***

**From a clean jolie-master SVN checkout:**
- All of the above
- In "Debug-configuration"
  - Add the following VM argument: `-Djava.library.path=/usr/local/lib/jni`
- Reference projects (see above):
  - unix.jar
  - dbus.jar
  - coreJavaServices.jar
  - monitorJavaServices.jar
