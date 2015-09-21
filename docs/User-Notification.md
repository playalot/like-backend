##Notification

消息中心列表

######URL

http://api.likeorz.com/v2/notification?ts=

######支持格式
JSON

######HTTP请求方式

GET

######请求参数
|参数|必选|类型及范围|说明|
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
|ts|true|long|时间戳|

######返回结果

######JSON示例

```json
{
    "code": 1,
    "message": "Record(s) Found",
    "data": {
        "notifications": [
            {
                "type": "LIKE",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 6118,
                    "content": "http://cdn.likeorz.com/78551c3d2cc84ee0896c736c01ee2bb6_1432897502_w_1536_h_1152_715.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "715",
                        "nickname": "Guan♏️",
                        "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                        "likes": 4765
                    }
                },
                "tags": [
                    "YAHOO",
                    "Aston Martin",
                    "买一辆喷like！"
                ],
                "timestamp": 1442584753
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 5578,
                    "content": "http://cdn.likeorz.com/6dd872e799d8ddd6_1432377477_w_550_h_366_172.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "172",
                        "nickname": "╮(╯▽╰)╭",
                        "avatar": "http://cdn.likeorz.com/avatar_172_1426758775.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tags": [
                    "吾王"
                ],
                "timestamp": 1442397276
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 5458,
                    "content": "http://cdn.likeorz.com/3e5c6651c13c4153_1432195916_w_553_h_738_90.png?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "90",
                        "nickname": "Skater",
                        "avatar": "http://cdn.likeorz.com/avatar_90_1423641357.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tags": [
                    "Boss"
                ],
                "timestamp": 1442397256
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 5416,
                    "content": "http://cdn.likeorz.com/335a17e3474b4f349687cf08f5728d4d_1432126537_w_1536_h_1152_122.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "122",
                        "nickname": "Ruri",
                        "avatar": "http://cdn.likeorz.com/avatar_122_1430613345.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tags": [
                    "基友"
                ],
                "timestamp": 1442397232
            },
            {
                "type": "COMMENT",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 5876,
                    "content": "http://cdn.likeorz.com/44c3dda9a4234207a9bc36ded32954b4_1432605822_w_1536_h_1536_715.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "715",
                        "nickname": "Guan♏️",
                        "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                        "likes": 4765
                    }
                },
                "tags": [
                    "love"
                ],
                "timestamp": 1441891182
            },
            {
                "type": "MARK",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 5876,
                    "content": "http://cdn.likeorz.com/44c3dda9a4234207a9bc36ded32954b4_1432605822_w_1536_h_1536_715.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "715",
                        "nickname": "Guan♏️",
                        "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                        "likes": 4765
                    }
                },
                "tags": [
                    "love6"
                ],
                "timestamp": 1441865214
            },
            {
                "type": "MARK",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 5876,
                    "content": "http://cdn.likeorz.com/44c3dda9a4234207a9bc36ded32954b4_1432605822_w_1536_h_1536_715.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "715",
                        "nickname": "Guan♏️",
                        "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                        "likes": 4765
                    }
                },
                "tags": [
                    "love6"
                ],
                "timestamp": 1441865167
            },
            {
                "type": "MARK",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 5876,
                    "content": "http://cdn.likeorz.com/44c3dda9a4234207a9bc36ded32954b4_1432605822_w_1536_h_1536_715.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "715",
                        "nickname": "Guan♏️",
                        "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                        "likes": 4765
                    }
                },
                "tags": [
                    "love6"
                ],
                "timestamp": 1441865134
            },
            {
                "type": "MARK",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 5876,
                    "content": "http://cdn.likeorz.com/44c3dda9a4234207a9bc36ded32954b4_1432605822_w_1536_h_1536_715.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "715",
                        "nickname": "Guan♏️",
                        "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                        "likes": 4765
                    }
                },
                "tags": [
                    "love5"
                ],
                "timestamp": 1441865128
            },
            {
                "type": "MARK",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 5876,
                    "content": "http://cdn.likeorz.com/44c3dda9a4234207a9bc36ded32954b4_1432605822_w_1536_h_1536_715.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "715",
                        "nickname": "Guan♏️",
                        "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                        "likes": 4765
                    }
                },
                "tags": [
                    "love5"
                ],
                "timestamp": 1441865074
            },
            {
                "type": "MARK",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 5876,
                    "content": "http://cdn.likeorz.com/44c3dda9a4234207a9bc36ded32954b4_1432605822_w_1536_h_1536_715.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "715",
                        "nickname": "Guan♏️",
                        "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                        "likes": 4765
                    }
                },
                "tags": [
                    "love4"
                ],
                "timestamp": 1441865022
            },
            {
                "type": "MARK",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 5876,
                    "content": "http://cdn.likeorz.com/44c3dda9a4234207a9bc36ded32954b4_1432605822_w_1536_h_1536_715.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "715",
                        "nickname": "Guan♏️",
                        "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                        "likes": 4765
                    }
                },
                "tags": [
                    "love3"
                ],
                "timestamp": 1441857448
            },
            {
                "type": "MARK",
                "user": {
                    "user_id": "187",
                    "nickname": "红果果",
                    "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                    "likes": 2513
                },
                "post": {
                    "post_id": 5876,
                    "content": "http://cdn.likeorz.com/44c3dda9a4234207a9bc36ded32954b4_1432605822_w_1536_h_1536_715.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "715",
                        "nickname": "Guan♏️",
                        "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                        "likes": 4765
                    }
                },
                "tags": [
                    "love2"
                ],
                "timestamp": 1441857256
            }
        ],
        "ts": 1441857256
    }
}

```

######返回字段说明
|返回值字段|字段类型|字段说明|
|--------|-------|-------|
|notes|array|消息列表|
|type|string|消息类型(LIKE MARK FOLLOW)|
|num|string|消息数量|
|is_new|int|是否是新消息|
|updated|int|消息产生时间|
|user|array|用户信息|
|user_id|int|用户ID|
|nickname|string|用户昵称|
|avatar|string|用户头像|
|posts|array|作品信息|
|content|string|作品缩略图|
|next|Long|下一页timestamp|