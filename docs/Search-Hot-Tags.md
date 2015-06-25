##Search Hot Tags

热门标签

######URL

http://api.likeorz.com/v1/tag/hot

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
    "data": [
        {
            "id": 7190,
            "tag": "超跑",
            "likes": 53,
            "image": "http://xxxxxx"
        },
        {
            "id": 1773,
            "tag": "家居",
            "likes": 54
        },
        {
            "id": 7372,
            "tag": "明日香",
            "likes": 132
        }
    ]
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|id|int|Tag ID|
|tag|string|标签名称|
|likes|int|点赞数|
|image|string|图片地址|

