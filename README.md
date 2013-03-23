D-Bus support for Jolie
============

***

**To simply work with the D-Bus extension from this repo:**
- Build using ant at source root `/jolie-src`
- Open the jolie NetBeans-project in `/jolie-src/jolie`
- In "Debug-configuration" 
  - Set working directory to: `/jolie-src/jolie/dist`
  - Add an argument for target jolie file to run e.g.: `../../playground/dbusSevice/server.ol`
  - Add the following argument to allow extensions to be loaded: `-l lib/*`

***

**To load additional extensions or libraries:**
- Reference their .jar files in the jolie project (Right-click on "Libraries" and choose "Add JAR/Folder..")

NOTE: If the .jar files of extensions such as dbus are loaded correctly (from their own `/dist` folder) the NetBeans debugger will be able to use the source to aid debugging.

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
