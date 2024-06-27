javac -d ./mogitServer/classes/ -sourcepath ./mogitServer/src/ $(find ./ -name "*.java")
java -cp ./mogitServer/classes/ mogitServer/src/com/example/apiserver/Main &
java -cp ./mogitServer/classes/ mogitServer/src/FileSendServer