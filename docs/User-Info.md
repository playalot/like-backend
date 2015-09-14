##GET User

查看用户界面 获取用户信息

######URL

http://api.likeorz.com/v1/user/{id:[0-9]+}

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|
|user/{id:[0-9]+}|true|int|用户ID|

######请求示例
<pre>
curl -i -X GET -H "LIKE-SESSION-TOKEN:bd625d6a6b6fe5bc0b128954ad5ca39d" http://api.likeorz.com/v2/user/1
</pre>

######返回结果

######JSON示例

```json
{
    "code": 1,
    "message": "Record(s) Found",
    "data": {
        "user_id": "11",
        "nickname": "你要怎样",
        "avatar": "http://cdn.likeorz.com/avatar_11_1422968054.jpg?imageView2/5/w/240",
        "origin_avatar": "http://cdn.likeorz.com/avatar_11_1422968054.jpg?imageView2/5/w/1242",
        "cover": "http://cdn.likeorz.com/cover_11_1430571065.jpg?imageView2/0/w/1242/h/1242/q/85",
        "likes": 57,
        "is_following": 0,
        "count": {
            "posts": 18,
            "following": 1,
            "followers": 1,
            "favorites": 10
        }
    }
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
