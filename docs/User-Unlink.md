##User Unlink Account

解除绑定第三方账号

######URL

http://api.likeorz.com/v1/account/link/:provider

######支持格式
JSON

######HTTP请求方式

DELETE

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
|provider|true|string|账号类型|


######返回结果

######JSON示例

```json
{
    "code": 1,
    "message": "解除绑定成功"
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
