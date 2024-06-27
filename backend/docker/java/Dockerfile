# ベースイメージとして openjdk:11-slim を使用
FROM openjdk:11-slim

# node をインストールするために必要な依存関係をインストール
RUN apt-get update && apt-get install -y curl gnupg && \
    curl -fsSL https://deb.nodesource.com/setup_14.x | bash - && \
    apt-get install -y nodejs

# 作業ディレクトリを /usr/src に設定
WORKDIR /usr/src

# Java サーバーの依存関係をインストール
COPY ./server/src /usr/src
RUN ./mvnw clean install

# Next.js アプリケーションのセットアップ
WORKDIR /usr/src/app
COPY ./server/app/package*.json ./
RUN npm install
COPY ./server/app ./
RUN npm run build

# Java サーバーをポート 8080 で、Next.js アプリケーションをポート 3000 で起動
EXPOSE 8080 3000

# 両方のサービスを並行して実行するスクリプトを追加
COPY start.sh /usr/src/start.sh
RUN chmod +x /usr/src/start.sh
CMD ["/usr/src/start.sh"]
