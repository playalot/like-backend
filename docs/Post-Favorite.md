##Post Favorite

收藏作品

######URL

http://api.likeorz.com/v1/post/{id:[0-9]+}/favorite

######支持格式
JSON

######HTTP请求方式

POST

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
|{id:[0-9]+}|true|int|作品ID|

######返回结果

######JSON示例

```json
{
    "code": "1", 
    "message": "Favorite Success"
}
```
