javac -d ../classes/ -sourcepath ./ $(find ./ -name "*.java")
java -cp ./mogitClient/classes/ mogitClient/src/FileSendClient