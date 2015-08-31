##Mark Comment

标签评论

######URL

http://api.likeorz.com/v1/mark/:id/comment

######支持格式
JSON

######HTTP请求方式

POST

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
|{id:[0-9]+}|true|int|标签ID|

######返回结果

######JSON示例

```json
{
    "code":1,
    "message":"Record(s) Found",
    "data":{
        "total_comments":1,
        "comments":[{
            "comment_id":18594,
            "content":"差个虎王",
            "created":1441010240,
            "place":"广州市",
            "user": {
                "user_id":14454,
                "nickname":"神神shadow",
                "avatar":"http://cdn.likeorz.com/avatar_14454_1440927143.jpg?imageView2/5/w/80",
                "likes":63
            },
            "reply":null
            }
        ]
    }
}
```
