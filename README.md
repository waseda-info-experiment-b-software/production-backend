## 環境構築
まずは `docker-compose up -d` にて Docker 環境を立てる

## コンテナ内のシェルにて

- backend 側 /usr/src で、

```shell
. mogit.sh
```

- 同様に frontend 側 /usr/src で、

```shell
. mogit.sh
```

- これを行ったのち、
  http://localhost:12000
  にアクセスすると、serverの内容が反映される。
