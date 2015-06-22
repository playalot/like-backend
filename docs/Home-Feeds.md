##Home

获取首页内容

######URL

http://api.likeorz.com/v2/homefeeds

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
            },
            {
                "post_id": 7605,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/3bb0dd1fa15a447a874023408744837d_1434216495_w_776_h_776_92.jpg?imageView2/1/w/620/h/620",
                "created": 1434216504,
                "marks": [
                    {
                        "mark_id": 24224,
                        "tag": "watch",
                        "likes": 3,
                        "is_liked": false
                    }
                ]
            },
            {
                "post_id": 8225,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/d2b655e2eea548feac158e06b42efba1_1434625890_w_1280_h_1579_368.jpg?imageView2/1/w/620/h/620",
                "created": 1434626024,
                "marks": [
                    {
                        "mark_id": 26905,
                        "tag": "明日香",
                        "likes": 3,
                        "is_liked": false
                    }
                ]
            },
            {
                "post_id": 8229,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/c4ee83c0c84b4a6c9b13454f7076b432_1434627280_w_1280_h_1281_872.jpg?imageView2/1/w/620/h/620",
                "created": 1434627358,
                "marks": [
                    {
                        "mark_id": 27084,
                        "tag": "唱片",
                        "likes": 2,
                        "is_liked": false
                    }
                ]
            },
            {
                "post_id": 8230,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/326ef6b663944362b2c98a81cabb52a7_1434627679_w_640_h_640_1125.jpg?imageView2/1/w/620/h/620",
                "created": 1434627691,
                "marks": [
                    {
                        "mark_id": 26928,
                        "tag": "耳机控",
                        "likes": 8,
                        "is_liked": false
                    },
                    {
                        "mark_id": 27245,
                        "tag": "不明觉厉",
                        "likes": 1,
                        "is_liked": false
                    }
                ]
            },
            {
                "post_id": 8231,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/22e6856d38b245ba92c7c7f7053d2025_1434628848_w_1194_h_1620_859.jpg?imageView2/1/w/620/h/620",
                "created": 1434628925,
                "marks": []
            },
            {
                "post_id": 8232,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/0ccd500e882a4bc881d909bf72059bba_1434629616_w_1280_h_1707_1053.jpg?imageView2/1/w/620/h/620",
                "created": 1434629648,
                "marks": [
                    {
                        "mark_id": 26934,
                        "tag": "啦啦",
                        "likes": 3,
                        "is_liked": false
                    },
                    {
                        "mark_id": 26935,
                        "tag": "路飞",
                        "likes": 6,
                        "is_liked": false
                    }
                ]
            },
            {
                "post_id": 8234,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/3361d8dc28344e20afbb8648e8e799c7_1434630043_w_1024_h_768_563.jpg?imageView2/1/w/620/h/620",
                "created": 1434630044,
                "marks": [
                    {
                        "mark_id": 26938,
                        "tag": "耳机控",
                        "likes": 8,
                        "is_liked": true
                    }
                ]
            },
            {
                "post_id": 8235,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/9c9f4ca2d5d34463a1cb201a8336d618_1434630115_w_640_h_640_567.jpg?imageView2/1/w/620/h/620",
                "created": 1434630116,
                "marks": [
                    {
                        "mark_id": 26940,
                        "tag": "OLYMPUS",
                        "likes": 6,
                        "is_liked": false
                    }
                ]
            },
            {
                "post_id": 8328,
                "type": "PHOTO",
                "content": "http://cdn.likeorz.com/37da64f0ac6b439a8cdea988138dd4f2_1434721551_w_1280_h_2276_679.jpg?imageView2/1/w/620/h/620",
                "created": 1434721579,
                "marks": [
                    {
                        "mark_id": 27322,
                        "tag": "哪里",
                        "likes": 1,
                        "is_liked": false
                    },
                    {
                        "mark_id": 27314,
                        "tag": "Geek的日常",
                        "likes": 2,
                        "is_liked": false
                    }
                ]
            }
        ],
        "next": "1434626024,1434721291,1434633872,1434212995"
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