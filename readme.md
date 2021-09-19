# 一括破壊mod  
SpigotでもMineAll, DigAll, CutAllの様に一括破壊ができるmodです。  

## 導入方法  
以下のコマンドを実行し、ビルドしてください。  
```
    git clone https://github.com/spigot-ikkatuhakai #リポジトリをクローン
    cd spigot-ikkatuhakai
    gradle jar #ビルド
```
実行後、build/libs下にjarファイルがあるのでこれをプラグインフォルダにコピーしてください。  

## 利用方法  
導入したサーバーにログインしたあと、/ikkatuコマンドが利用できます。  
コマンド一覧  
/ikkatu cut,mine,dig,all on,off,toggle  
対応したもののon,offを切り替えます。2番目の引数を空にすると現在の設定が確認できます。

/ikkatu reload  
configを再読込します。

## configの設定  
初回の起動後、plugins/ikkatuhakaiフォルダ下にconfigファイル、cut.yml, dig.yml, mine.ymlが生成されます。  

### 設定一覧  
leaves: cutall用。葉ブロックを指定する。  
blocks: 一括破壊の対象のブロック  
tools: 一括破壊で利用する道具  
limit: 一括破壊での連鎖上限  
defaultstatus: ログイン時のディフォルトの設定。標準でOFF

blocks, tools, leavesに指定できるブロック,アイテム一覧は[ここ](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html "名称一覧") から参照できます。