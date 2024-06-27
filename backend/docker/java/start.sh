#!/bin/sh

# Java サーバーをバックグラウンドで起動
java -jar /usr/src/target/myapp.jar &

# Next.js アプリケーションを起動
npm start --prefix /usr/src/app
