##Mark GET

标签详情

######URL

http://api.likeorz.com/v1/mark/{id:[0-9]+}

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
    code: 1,
    message: "Record(s) Found",
    data: {
        mark_id: "11111",
        user: {
        user_id: 73,
        nickname: "晞婷",
        avatar: "http://cdn.likeorz.com/avatar_73_1423579852.jpg?imageView2/5/w/80",
        likes: "1597"
    },
    tag: "破洞",
    likes: 2,
    is_liked: false,
    created: 1431054710
    }
}
```
