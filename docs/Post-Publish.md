##Post Publish

发表内容

######URL

http://api.likeorz.com/v1/post

######支持格式
JSON

######HTTP请求方式

POST

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
|content|true|string|内容或资源链接（七牛文件名）|
|description|false|string|内容描述 |
|type|true|false|类型（暂时只支持 PHOTO）|
|tags|true|array|选定的标签数组（格式化成json格式）|

######请求示例json
```json
{
    "content": "42a477c5614e0fed_1427805894_w_428_h_640_181.jpg",
    "description(optional)": "xxxxxxxxx",
    "type(optional)": "PHOTO",
    "tags":["tag1", "tag2"]
}
```

######返回结果

######JSON示例

```json
{
    "code": 1,
    "message": "发布图片成功",
    "data": {
        "post_id": 5451,
        "content": "http://cdn.likeorz.com/42a477c5614e0fed_1427805894_w_428_h_640_181.jpg?imageView2/1/w/620/h/620",
        "type": "PHOTO",
        "description": null,
        "created": 1434585248,
        "user": {
            "user_id": "826",
            "nickname": "Guan Guan",
            "avatar": "http://cdn.likeorz.com/default_avatar.jpg?imageView2/1/w/80/h/80",
            "likes": 0
        },
        "marks": [
            {
                "mark_id": 15583,
                "tag": "tag2",
                "likes": 1,
                "is_liked": 1
            },
            {
                "mark_id": 15584,
                "tag": "tag3",
                "likes": 1,
                "is_liked": 1
            }
        ]
    }
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|post_id|int|内容ID|
|content|string|内容|
|type|string|类型|
|description|string|描述|
|created|int|发表时间|
|user|array|用户信息|
|user_id|int|用户ID|
|nickname|string|用户昵称|
|avatar|string|用户头像|
|likes|int|用户总赞数|