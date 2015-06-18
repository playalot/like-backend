##User SendCode

发送短信验证码

######URL

http://api.likeorz.com/v1/user/sendCode

######支持格式
JSON

######HTTP请求方式

POST

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|mobile|true|string|手机号|

######请求示例json
```json
{
    "zone": "86",
    "mobile": "13333333333"
}
```

######返回结果

######JSON示例

```json
{
    "code": "1",
    "message": "Code Send Success"
}
```