##GET Post

帖子详情

######URL

http://api.likeorz.com/v1/post/{id:[0-9]+}

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|
|{id:[0-9]+}|true|int|作品ID|

######请求示例
<pre>
curl -X GET -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39"  http://api.likeorz.com/v1/post/3036
</pre>
######返回结果

######JSON示例

```json
{
    "code": "1", 
    "message": "Record(s) Found", 
    "data": {
        "post_id": "3036", 
        "type": "PHOTO", 
        "content": "http://storage.likeorz.com/42a477c5614e0fed_1427805894_w_428_h_640_181.jpg", 
        "description": "09R1……", 
        "created": "1427805894",
        "favorited": false,
        "place(optional)": "Beijing",
        "location(optional)": [11.11, 22.22],
        "is_favored": "0", 
        "user": {
            "user_id": "181", 
            "nickname": "京城-宝嘚", 
            "avatar": "http://storage.likeorz.com/avatar_181_1427523684.jpg", 
            "likes": "178"
        }
    }
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|post_id|int|内容ID|
|type|string|类型|
|content|string|内容|
|description|string|描述|
|favorite|bool|是否已经保存|
|created|int|发布时间|
|user|array|用户信息|
|user_id|int|用户ID|
|nickname|string|用户昵称|
|avatar|string|用户头像|
|likes|int|用户总赞数|