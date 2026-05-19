# [P1] .gitignore が不十分で、IDE/OS固有ファイル・ビルド成果物がコミットされている

## 概要
`.gitignore` が `.gradle` と `build` の2行しかなく、Eclipse 系ファイル（`.classpath` `.project` `.settings/`）、macOS の `.DS_Store`、コンパイル済みクラス（`bin/`）がそのままコミットされています。チーム開発で他人のIDE設定と衝突する原因になりますし、`.class` ファイルが履歴に残るとリポジトリが肥大化します。

## 該当箇所
`samplePJ/.gitignore:1-6`
```
# Ignore Gradle project-specific cache directory
.gradle

# Ignore Gradle build output directory
build
```

実際にコミットされている問題ファイル例:
- `samplePJ/.DS_Store`
- `samplePJ/app/.DS_Store`
- `samplePJ/app/src/.DS_Store`
- `samplePJ/.classpath` / `samplePJ/.project` / `samplePJ/.settings/`
- `samplePJ/app/.classpath` / `samplePJ/app/.project` / `samplePJ/app/.settings/`
- `samplePJ/bin/`（コンパイル済み `.class` ファイル群）
- `samplePJ/app/bin/`

## 何が問題か
1. `.DS_Store` は macOS 専用のメタファイルで、リポジトリに入る理由がありません。
2. `.classpath` / `.project` / `.settings/` は Eclipse 固有。他人がIntelliJ などで開いたとき邪魔になり、Eclipseのバージョン差分でも頻繁にdiffが出ます。
3. `bin/` の `.class` ファイルはビルドすれば再生成されるもの。リポジトリの肥大化と不要なコンフリクトを招きます。
4. `.env` や本番用 properties をこの先追加した時に巻き込み事故が起こりやすいです。

## 修正方針
`.gitignore` を以下に置き換え（Spring Boot + Gradle + Eclipse + macOS の標準的な内容）

```gitignore
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar

# Eclipse
.classpath
.project
.settings/
bin/

# IntelliJ IDEA
.idea/
*.iml
*.iws

# VS Code
.vscode/

# macOS
.DS_Store

# Logs / env
*.log
.env
.env.local
```

既にコミット済みのファイルは `.gitignore` だけでは消えないので、以下も実施します。
```bash
git rm -r --cached samplePJ/.classpath samplePJ/.project samplePJ/.settings \
    samplePJ/app/.classpath samplePJ/app/.project samplePJ/app/.settings \
    samplePJ/bin samplePJ/app/bin
find . -name '.DS_Store' -exec git rm --cached {} \;
git commit -m "chore: stop tracking IDE/OS specific files and build outputs"
```

## 検証
- `git status --ignored` で `.DS_Store` `bin/` などが Ignored 一覧に入ること
- 別のマシン（あるいはVSCode）で clone → ビルドして問題なく動くこと

## 関連
- Issue 04（認証情報のハードコード）
