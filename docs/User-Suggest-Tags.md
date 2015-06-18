##User Suggest

获取用户相关度最高的标签

######URL

http://api.likeorz.com/v1/user/suggest

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|

######返回结果

######JSON示例

```json
{
    "code": "1", 
    "message": "Record(s) Found", 
    "data": {
        "suggests": [
            {
                "tag": "测试一下"
            }, 
            {
                "tag": "测试标签"
            }, 
            {
                "tag": "标签3"
            }, 
            {
                "tag": "标签2"
            }, 
            {
                "tag": "标签1"
            }, 
            {
                "tag": "豪车"
            }, 
            {
                "tag": "门前大桥下"
            }, 
            {
                "tag": "胆机"
            }, 
            {
                "tag": "bewitch"
            }, 
            {
                "tag": "KT88"
            }, 
            {
                "tag": "酒店"
            }, 
            {
                "tag": "我也想吃"
            }, 
            {
                "tag": "喵星人"
            }, 
            {
                "tag": "镜头"
            }, 
            {
                "tag": "黑胶唱片"
            }, 
            {
                "tag": "松鼠"
            }, 
            {
                "tag": "剪发"
            }, 
            {
                "tag": "山鸡"
            }, 
            {
                "tag": "FF-10"
            }, 
            {
                "tag": "刻录光盘"
            }
        ]
    }
}
```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|tag|string|推荐给用户的标签|