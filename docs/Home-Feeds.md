##Home

获取首页内容

######URL

http://api.likeorz.com/v1/homeFeeds

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|false|string|Header中带用户授权|
|ts|true|String|分页|

######请求示例
```
curl -X GET -H "LIKE-SESSION-TOKEN: c5509917fcc2b870e5b4eabd4de7cd39"  http://api.likeorz.com/v2/homefeeds?ts=1434630256,1434723547,1434642983,1434256998
```
######返回结果

######JSON示例

```json
{
    "code": 1,
    "message": "Record(s) Found",
    "data": {
        "posts": [
            {
                "post_id": 7593,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/67043fc620684b22ad9aec901f697d3c_1434212994_w_1280_h_1707_187.jpg?imageView2/1/w/620/h/620",
                "created": 1434212995,
                "user":{
                    "user_id": 715,
                    "nickname": "Guan♏️",
                    "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                    "likes": "4759"
                },
                "marks": [
                    {
                        "mark_id": 24184,
                        "tag": "打火机",
                        "likes": 3,
                        "is_liked": false
                    },
                    {
                        "mark_id": 24206,
                        "tag": "Zippo",
                        "likes": 10,
                        "is_liked": false
                    }
                ]
            },
            {
                "post_id": 7596,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/fec36feac2e14c3eaf20ece48da4dfcc_1434214517_w_640_h_427_97.jpg?imageView2/1/w/620/h/620",
                "created": 1434214522,
                "user":{
                    "user_id": 715,
                    "nickname": "Guan♏️",
                    "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                    "likes": "4759"
                },
                "marks": [
                    {
                        "mark_id": 24219,
                        "tag": "酒鬼",
                        "likes": 3,
                        "is_liked": false
                    },
                    {
                        "mark_id": 24193,
                        "tag": "收藏",
                        "likes": 2,
                        "is_liked": false
                    }
                ]
            }
        ],
        "next": "1434626024"
    }
}

```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|post_id|int|内容ID|
|type|string|类型|
|content|string|内容|
|created|int|发布时间|
|user|array|用户信息|
|user_id|int|用户ID|
|nickname|string|用户昵称|
|avatar|string|用户头像|
|likes|int|用户获得赞数|
|marks|array|标签信息|
|mark_id|int|标签ID|
|tag|string|标签名|
|likes|int|点赞数|
|is_liked|int|当前访问用户是否赞过|
|next|String|下一页timestamp组合|