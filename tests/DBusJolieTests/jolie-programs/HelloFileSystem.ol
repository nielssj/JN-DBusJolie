include "file.iol"

main
{
  f.filename = "MyFile.txt";
  f.content = "Hello file system!";
  writeFile@File(f)()
}
