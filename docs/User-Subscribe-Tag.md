##Tag Following

订阅标签

######URL

http://api.likeorz.com/v1/tag/:id/subscribe

######支持格式
JSON

######HTTP请求方式

POST

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|
|id|true|int|标签名称|


######请求示例
<pre>
curl -X POST -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39"  http://api.likeorz.com/v1/tag/100/subscribe
</pre>
######返回结果

######JSON示例

```json
{
    "code": "1", 
    "message": "Subscribe Tag Success"
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
