# Spring boot + MyBatis による会議室予約システム

## 内容

Spring Boot + MyBatis + JPA + Thymeleaf + Security で会議室予約の Web アプリを作成しました。
概要は以下の通りです。

- 会議室の一覧が表示される
- 会議室の予約について
  - 会議室と日付ごとに予約状況を確認できる
  - 会議室、日付、時間帯は重複できない
  - 通知日、通知時刻、参加人数、メモの入力は任意
- アプリは CRUD 機能を持つ
- ログ出力を行う
- ログイン機能を持つ。
- DB は docker で起動した postgreSQL に接続する。
- 単体テストでは H2DB を使用する。

## 注意点

- etc/docker フォルダで"docker-compose up"することで DB を起動できる。

## 確認方法

Spring Boot アプリケーション起動後、以下の URL で起動できます。

<http://localhost:8080/>
