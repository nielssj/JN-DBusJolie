JN-DBusJolie
============


To simply work with the D-Bus extension from this repo:
-------------

- build with ant at source root "/jolie-src"
- Open the NetBeans project in "/jolie-src/jolie"
- In "Debug-configuration" 
  - Set working directory to: "/jolie-src/jolie/dist"
  - Add the following argument: "-l lib/*"
  - Add an argument for target jolie file to run, for example: "../../playground/dbusSevice/server.ol"
  

Additional tweaks from a clean jolie-master SVN checkout:
-------------
- In "Debug-configuration"
  - Add the following VM argument: "-Djava.library.path=/usr/local/lib/jni"
- Reference projects (see how below):
  - unix.jar
  - debus.jar
  - coreJavaServices.jar
  - monitorJavaServices.jar


To load additional extensions or libraries:
-------------

- Reference their .jar files in the jolie project (Right-click on "Libraries" and choose "Add JAR/Folder.."


NOTE: If the .jar files of extensions such as dbus are loaded correctly from its own /dist folder the NetBeans debugger will be able to use the source to aid debugging.