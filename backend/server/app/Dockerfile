# backend/server/app/Dockerfile

# ベースイメージとして node:14 を使用
FROM node:14

# 作業ディレクトリを /app に設定
WORKDIR /app

# package.json と package-lock.json をコピー
COPY package*.json ./

# 依存関係をインストール
RUN npm install

# ソースコードをコピー
COPY . .

# ビルドを実行
RUN npm run build

# Next.js サーバーを起動
CMD ["npm", "start"]

# コンテナがリッスンするポートを指定
EXPOSE 3000