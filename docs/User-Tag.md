##User Following

获取用户关注列表

######URL

http://api.likeorz.com/v1/tag/:name/subscribe

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|
|name|true|string|标签名称|


######请求示例
<pre>
curl -X GET -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39"  http://api.likeorz.com/v1/tag/高达/subscribe
</pre>
######返回结果

######JSON示例

```json
{
    "code": 1,
    "message": "Record(s) Found",
    "data":{
        "id": 100,
        "tag": "高达",
        "subscribed": true
    }
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|