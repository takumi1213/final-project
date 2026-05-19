# [P2] `Task.name`（登録者名）の二重管理、`createdAt` をDB任せにする

## 概要
`Task` には `userId` と別に `name`（登録者名）があり、フォームで手入力させる作りになっています。ログインユーザーが分かっているのにフォームから別途登録者名を取る理由がなく、改ざんも容易です。また、`createdAt` は INSERT 時にDB側で自動付与されない設計になっています（`DEFAULT CURRENT_TIMESTAMP` が無いと NULL になる）。

## 該当箇所
`samplePJ/app/src/main/resources/sample/common/dao/mapper/TaskMapper.xml:42-48`
```xml
<insert id="insert" parameterType="sample.common.dao.entity.Task">
    INSERT INTO tasks (
        user_id, title, content, name, start_date, end_date
    ) VALUES (
        #{userId}, #{title}, #{content}, #{name}, #{startDate}, #{endDate}
    )
</insert>
```
`samplePJ/app/src/main/resources/templates/tasks/new.html:106-108`
```html
<label>登録者</label>
<input type="text" th:field="*{name}">
```

## 何が問題か
1. 登録者はログインユーザーから一意に決まるのに、フォーム入力させると別人の名前を書ける状態になります。
2. ユーザー名変更を将来サポートしたいとき、`tasks.name` と `login.username` の二重更新が必要になりがちです。データ正規化の観点で、`name` カラムは不要で、`user_id` の JOIN で表示すれば良いです。
3. `createdAt` `updatedAt` がDDLに `DEFAULT CURRENT_TIMESTAMP` を持っているかDDL側を確認するか、INSERT/UPDATE文側で明示的に `CURRENT_TIMESTAMP` を入れる必要があります。`update` 側は入れているのにINSERT側は無いという不整合があります。

## 修正方針
**短期対応（DB変更なし）**
- `new.html` の登録者入力欄を削除
- Controller で `task.setName(ログインユーザー名)` をセット（ログイン時にusernameもセッションに入れておく）
- `insert` 文に `created_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP` を追加

```xml
<insert id="insert" parameterType="sample.common.dao.entity.Task">
    INSERT INTO tasks (
        user_id, title, content, name, start_date, end_date, created_at, updated_at
    ) VALUES (
        #{userId}, #{title}, #{content}, #{name}, #{startDate}, #{endDate},
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    )
</insert>
```

**中期対応（理想）**
- `tasks.name` カラム削除、`tasks.user_id` + `login.username` の JOIN で表示
- 一覧取得時に `LEFT JOIN login ON login.id = tasks.user_id` してusernameを取得

```xml
<select id="findPageByUserId" resultType="sample.common.dao.entity.Task">
    SELECT 
        t.id, 
        t.user_id AS userId, 
        t.title, 
        t.content, 
        l.username AS name,
        t.start_date AS startDate, 
        t.end_date AS endDate, 
        t.created_at AS createdAt, 
        t.updated_at AS updatedAt
    FROM tasks t
    LEFT JOIN login l ON l.id = t.user_id
    WHERE t.user_id = #{userId}
    ORDER BY t.created_at DESC 
    LIMIT #{limit} OFFSET #{offset}
</select>
```

## 検証
- 登録者が改ざんできないこと（DevToolsで `name` を別名に書き換え送信しても保存されない）
- 一覧で登録者が常にログインユーザー名になっていること
- 新規登録直後のレコードで `created_at` `updated_at` が NULL でないこと

## 関連
- Issue 06（Form/DTO分離）
- Issue 09（バリデーション）
