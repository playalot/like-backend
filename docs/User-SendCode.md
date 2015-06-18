##User SendCode

发送短信验证码

######URL

http://api.likeorz.com/v1/sendSmsCode

######支持格式
JSON

######HTTP请求方式

POST

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|-------|
|mobile|true|string|手机号|
|zone|true|string|国家代号|
|token|true|string|加密后的字符串|

######请求示例json
```json
{
    "zone": "CN",
    "mobile": "13333333333",
    "token" "EA42ED1CE627F0703EA888C3245B84C2"
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