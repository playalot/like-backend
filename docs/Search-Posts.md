##Search Posts

搜索图片

######URL

http://api.likeorz.com/v1/search/tag/:tag/:page

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|

######返回结果

######JSON示例

```json
{
    "code": 1,
    "message": "Record(s) Found",
    "data": {
        "info": {
            "id": 123,
            "name": "xxxx",
            "description": "xxxxxxxx",
            "avatar": "avatar_123_xxxxxxx.jpg"
        },
        "posts": [
            {
                "post_id": 8298,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/2e4f809358554802af9e28011dd59c09_1434688190_w_640_h_852_401.jpg?imageView2/1/w/620/h/620",
                "created": 1434688257,
                "user": {
                    "user_id": 401,
                    "nickname": "Kamiko丸",
                    "avatar": "http://cdn.likeorz.com/avatar_401_1435058689.jpg?imageView2/1/w/80/h/80",
                    "likes": 6105
                }
            },
            {
                "post_id": 8332,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/b3e6ad1a26d740a8910d4d985d0bcde8_1434723645_w_640_h_640_110.jpg?imageView2/1/w/620/h/620",
                "created": 1434723646,
                "user": {
                    "user_id": 110,
                    "nickname": "Amo",
                    "avatar": "http://cdn.likeorz.com/avatar_110_1423842618.jpg?imageView2/1/w/80/h/80",
                    "likes": 484
                }
            }
        ]
    }
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|id|int|Tag ID|
|tag|string|标签名称|
|likes|int|点赞数|
|image|string|图片地址|
