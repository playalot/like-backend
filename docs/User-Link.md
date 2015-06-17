##User Link Account

绑定第三方账号

######URL

http://api.likeorz.com/v1/account/link/:provider

######支持格式
JSON

######HTTP请求方式

POST

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
|provider|true|string|账号类型|
|uid|true|string|第三方账号ID|
|access_token|true|string|第三方账号授权码|

######请求示例json
```json
{
    "uid": "13333333333",
    "access_token": "12341231231"
}
```


######返回结果

######JSON示例

```json
{
    "code": 1,
    "message": "绑定成功",
    "data": {
        "user_id" -> "123",
        "provider_id" -> "weibo",
        "provider_key" -> "weibo_id"
    }
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|user_id|string|用户ID|
|provider_id|string|账户类型|
|provider_key|string|第三方账户ID|