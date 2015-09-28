##User Fans

获取用户粉丝

######URL

http://api.likeorz.com/v1/user/{id:[0-9]+}/followers/{page:[0-9]+}

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|
|{id:[0-9]+}|true|int|用户ID|
|{page:[0-9]+}|true|int|分页|

######请求示例
<pre>
curl -X GET -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39"  http://api.likeorz.com/v1/user/1/fans/0
</pre>
######返回结果

######JSON示例

```json
{
    "code": "1", 
    "message": "Record(s) Found", 
    "data": {
        "fans": [
            {
                "user_id": "173", 
                "nickname": "akaaa", 
                "avatar": "http://storage.likeorz.com/avatar_173_1426760459.jpg", 
                "likes": "229",
                "is_following": true
            }, 
            {
                "user_id": "53", 
                "nickname": "Ranger °", 
                "avatar": "http://storage.likeorz.com/avatar_53_1426382398.jpg", 
                "likes": "263",
                "is_following": false
            }, 
        ]
    }
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|user_id|int|用户ID|
|nickname|string|昵称|
|avatar|string|头像|
|likes|int|点赞数|
|is_following|bool|互相关注|