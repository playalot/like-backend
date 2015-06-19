##Mark Likes

标签赞列表

######URL

http://api.likeorz.com/v1/mark/{id:[0-9]+}/likes

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|{id:[0-9]+}|true|int|标签ID|

######返回结果

######JSON示例

```json
{
    "code": "1", 
    "message": "Record(s) Found", 
    "data": {
        "likes": [
            {
                "user": {
                    "user_id": "2", 
                    "nickname": "北小生", 
                    "avatar": "http://storage.likeorz.com/avatar_2_1422995391.jpg", 
                    "likes": "68"
                }
            }, 
            {
                "user": {
                    "user_id": "3", 
                    "nickname": "Like", 
                    "avatar": "http://storage.likeorz.com/avatar_3_1427032014.jpg", 
                    "likes": "423"
                }
            }, 
            {
                "user": {
                    "user_id": "1", 
                    "nickname": "小改", 
                    "avatar": "http://storage.likeorz.com/avatar_8.jpg", 
                    "likes": "362"
                }
            }, 
            {
                "user": {
                    "user_id": "178", 
                    "nickname": "果布斯????", 
                    "avatar": "http://storage.likeorz.com/avatar_178_1426989853.jpg", 
                    "likes": "226"
                }
            }, 
            {
                "user": {
                    "user_id": "25", 
                    "nickname": "兔兔", 
                    "avatar": "http://storage.likeorz.com/avatar_25_1423545701.jpg", 
                    "likes": "57"
                }
            }
        ]
    }
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|user_id|int|用户ID|
|nickname|string|用户昵称|
|avatar|string|用户头像|
|likes|int|用户获得赞数|