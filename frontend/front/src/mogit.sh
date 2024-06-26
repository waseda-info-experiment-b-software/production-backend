javac -d ./mogitClient/classes/ -sourcepath ./mogitClient/src/ $(find ./ -name "*.java")
java -cp ./mogitClient/classes/ mogitClient/src/FileSendClient