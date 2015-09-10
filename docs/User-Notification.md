##Notification

消息中心列表

######URL

http://api.likeorz.com/v1/notification?ts=

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
                "type": "COMMENT",
                "user": {
                    "user_id": "16682",
                    "nickname": "小白白白",
                    "avatar": "http://cdn.likeorz.com/avatar_16682_1441858709.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 52931,
                        "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "11226",
                            "nickname": "Elkins",
                            "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                            "likes": 488
                        }
                    }
                ],
                "post": {
                    "post_id": 52931,
                    "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "11226",
                        "nickname": "Elkins",
                        "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                        "likes": 488
                    }
                },
                "tag": "朝阳群众",
                "mark_id": 255482,
                "timestamp": 1441866556
            },
            {
                "type": "COMMENT",
                "user": {
                    "user_id": "16682",
                    "nickname": "小白白白",
                    "avatar": "http://cdn.likeorz.com/avatar_16682_1441858709.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 52931,
                        "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "11226",
                            "nickname": "Elkins",
                            "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                            "likes": 488
                        }
                    }
                ],
                "post": {
                    "post_id": 52931,
                    "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "11226",
                        "nickname": "Elkins",
                        "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                        "likes": 488
                    }
                },
                "tag": "朝阳群众",
                "mark_id": 255482,
                "timestamp": 1441865720
            },
            {
                "type": "COMMENT",
                "user": {
                    "user_id": "16682",
                    "nickname": "小白白白",
                    "avatar": "http://cdn.likeorz.com/avatar_16682_1441858709.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 52931,
                        "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "11226",
                            "nickname": "Elkins",
                            "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                            "likes": 488
                        }
                    }
                ],
                "post": {
                    "post_id": 52931,
                    "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "11226",
                        "nickname": "Elkins",
                        "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                        "likes": 488
                    }
                },
                "tag": "朝阳群众",
                "mark_id": 255482,
                "timestamp": 1441864914
            },
            {
                "type": "FOLLOW",
                "user": {
                    "user_id": "16682",
                    "nickname": "小白白白",
                    "avatar": "http://cdn.likeorz.com/avatar_16682_1441858709.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                ],
                "timestamp": 1441864362
            },
            {
                "type": "COMMENT",
                "user": {
                    "user_id": "10664",
                    "nickname": "Gson",
                    "avatar": "http://cdn.likeorz.com/avatar_10664_1440917454.jpg?imageView2/5/w/80",
                    "likes": 42
                },
                "posts": [
                    {
                        "post_id": 52931,
                        "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "11226",
                            "nickname": "Elkins",
                            "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                            "likes": 488
                        }
                    }
                ],
                "post": {
                    "post_id": 52931,
                    "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "11226",
                        "nickname": "Elkins",
                        "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                        "likes": 488
                    }
                },
                "tag": "好",
                "mark_id": 414813,
                "timestamp": 1441853297
            },
            {
                "type": "COMMENT",
                "user": {
                    "user_id": "10664",
                    "nickname": "Gson",
                    "avatar": "http://cdn.likeorz.com/avatar_10664_1440917454.jpg?imageView2/5/w/80",
                    "likes": 42
                },
                "posts": [
                    {
                        "post_id": 52931,
                        "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "11226",
                            "nickname": "Elkins",
                            "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                            "likes": 488
                        }
                    }
                ],
                "post": {
                    "post_id": 52931,
                    "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "11226",
                        "nickname": "Elkins",
                        "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                        "likes": 488
                    }
                },
                "tag": "好",
                "mark_id": 414813,
                "timestamp": 1441853236
            },
            {
                "type": "MARK",
                "user": {
                    "user_id": "166",
                    "nickname": "Demo",
                    "avatar": "http://cdn.likeorz.com/avatar_166_1433409512.jpg?imageView2/5/w/80",
                    "likes": 41
                },
                "posts": [
                    {
                        "post_id": 52931,
                        "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "11226",
                            "nickname": "Elkins",
                            "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                            "likes": 488
                        }
                    }
                ],
                "post": {
                    "post_id": 52931,
                    "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "11226",
                        "nickname": "Elkins",
                        "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                        "likes": 488
                    }
                },
                "tag": "好",
                "mark_id": null,
                "timestamp": 1441801141
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "16376",
                    "nickname": "煤气中毒的猫",
                    "avatar": "http://cdn.likeorz.com/avatar_16376_1441763439.jpg?imageView2/5/w/80",
                    "likes": 294
                },
                "posts": [
                    {
                        "post_id": 52931,
                        "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "11226",
                            "nickname": "Elkins",
                            "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                            "likes": 488
                        }
                    }
                ],
                "post": {
                    "post_id": 52931,
                    "content": "http://cdn.likeorz.com/9d21f54eae5c46f7a8c02c45809a3bca_1440143030_w_1280_h_960_11226.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "11226",
                        "nickname": "Elkins",
                        "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                        "likes": 488
                    }
                },
                "tag": "朝阳群众",
                "mark_id": null,
                "timestamp": 1441796753
            },
            {
                "type": "COMMENT",
                "user": {
                    "user_id": "715",
                    "nickname": "Guan♏️",
                    "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                    "likes": 7649
                },
                "posts": [
                    {
                        "post_id": 60846,
                        "content": "http://cdn.likeorz.com/0c5f3f81c8804679b6917d5b369c8c54_1440670853_w_1280_h_1707_11226.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "11226",
                            "nickname": "Elkins",
                            "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                            "likes": 488
                        }
                    }
                ],
                "post": {
                    "post_id": 60846,
                    "content": "http://cdn.likeorz.com/0c5f3f81c8804679b6917d5b369c8c54_1440670853_w_1280_h_1707_11226.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "11226",
                        "nickname": "Elkins",
                        "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                        "likes": 488
                    }
                },
                "tag": "测试",
                "mark_id": 301799,
                "timestamp": 1441794763
            },
            {
                "type": "COMMENT",
                "user": {
                    "user_id": "715",
                    "nickname": "Guan♏️",
                    "avatar": "http://cdn.likeorz.com/avatar_715_1436330633.jpg?imageView2/5/w/80",
                    "likes": 7649
                },
                "posts": [
                    {
                        "post_id": 60846,
                        "content": "http://cdn.likeorz.com/0c5f3f81c8804679b6917d5b369c8c54_1440670853_w_1280_h_1707_11226.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "11226",
                            "nickname": "Elkins",
                            "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                            "likes": 488
                        }
                    }
                ],
                "post": {
                    "post_id": 60846,
                    "content": "http://cdn.likeorz.com/0c5f3f81c8804679b6917d5b369c8c54_1440670853_w_1280_h_1707_11226.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "11226",
                        "nickname": "Elkins",
                        "avatar": "http://cdn.likeorz.com/avatar_11226_1441700786.jpg?imageView2/5/w/80",
                        "likes": 488
                    }
                },
                "tag": "测试",
                "mark_id": 301799,
                "timestamp": 1441794724
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "14176",
                    "nickname": "狼",
                    "avatar": "http://cdn.likeorz.com/avatar_14176_1440892780.jpg?imageView2/5/w/80",
                    "likes": 25
                },
                "posts": [
                    {
                        "post_id": 76355,
                        "content": "http://cdn.likeorz.com/373828ad9320499bb9ac89a3f447fbbd_1441710493_w_826_h_826_6692.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "6692",
                            "nickname": "拆盒网岑岑",
                            "avatar": "http://cdn.likeorz.com/avatar_6692_1441725536.jpg?imageView2/5/w/80",
                            "likes": 46
                        }
                    }
                ],
                "post": {
                    "post_id": 76355,
                    "content": "http://cdn.likeorz.com/373828ad9320499bb9ac89a3f447fbbd_1441710493_w_826_h_826_6692.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "6692",
                        "nickname": "拆盒网岑岑",
                        "avatar": "http://cdn.likeorz.com/avatar_6692_1441725536.jpg?imageView2/5/w/80",
                        "likes": 46
                    }
                },
                "tag": "红玫瑰",
                "mark_id": null,
                "timestamp": 1441754480
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "14176",
                    "nickname": "狼",
                    "avatar": "http://cdn.likeorz.com/avatar_14176_1440892780.jpg?imageView2/5/w/80",
                    "likes": 25
                },
                "posts": [
                    {
                        "post_id": 76355,
                        "content": "http://cdn.likeorz.com/373828ad9320499bb9ac89a3f447fbbd_1441710493_w_826_h_826_6692.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "6692",
                            "nickname": "拆盒网岑岑",
                            "avatar": "http://cdn.likeorz.com/avatar_6692_1441725536.jpg?imageView2/5/w/80",
                            "likes": 46
                        }
                    }
                ],
                "post": {
                    "post_id": 76355,
                    "content": "http://cdn.likeorz.com/373828ad9320499bb9ac89a3f447fbbd_1441710493_w_826_h_826_6692.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "6692",
                        "nickname": "拆盒网岑岑",
                        "avatar": "http://cdn.likeorz.com/avatar_6692_1441725536.jpg?imageView2/5/w/80",
                        "likes": 46
                    }
                },
                "tag": "纸玫瑰",
                "mark_id": null,
                "timestamp": 1441754479
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "16263",
                    "nickname": "Sdz555",
                    "avatar": "http://cdn.likeorz.com/avatar_16263_1441695121.jpg?imageView2/5/w/80",
                    "likes": 156
                },
                "posts": [
                    {
                        "post_id": 76355,
                        "content": "http://cdn.likeorz.com/373828ad9320499bb9ac89a3f447fbbd_1441710493_w_826_h_826_6692.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "6692",
                            "nickname": "拆盒网岑岑",
                            "avatar": "http://cdn.likeorz.com/avatar_6692_1441725536.jpg?imageView2/5/w/80",
                            "likes": 46
                        }
                    }
                ],
                "post": {
                    "post_id": 76355,
                    "content": "http://cdn.likeorz.com/373828ad9320499bb9ac89a3f447fbbd_1441710493_w_826_h_826_6692.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "6692",
                        "nickname": "拆盒网岑岑",
                        "avatar": "http://cdn.likeorz.com/avatar_6692_1441725536.jpg?imageView2/5/w/80",
                        "likes": 46
                    }
                },
                "tag": "纸玫瑰",
                "mark_id": null,
                "timestamp": 1441731385
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "3808",
                    "nickname": "金雅",
                    "avatar": "http://cdn.likeorz.com/avatar_3808_1437064114.jpg?imageView2/5/w/80",
                    "likes": 262
                },
                "posts": [
                    {
                        "post_id": 76355,
                        "content": "http://cdn.likeorz.com/373828ad9320499bb9ac89a3f447fbbd_1441710493_w_826_h_826_6692.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "6692",
                            "nickname": "拆盒网岑岑",
                            "avatar": "http://cdn.likeorz.com/avatar_6692_1441725536.jpg?imageView2/5/w/80",
                            "likes": 46
                        }
                    }
                ],
                "post": {
                    "post_id": 76355,
                    "content": "http://cdn.likeorz.com/373828ad9320499bb9ac89a3f447fbbd_1441710493_w_826_h_826_6692.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "6692",
                        "nickname": "拆盒网岑岑",
                        "avatar": "http://cdn.likeorz.com/avatar_6692_1441725536.jpg?imageView2/5/w/80",
                        "likes": 46
                    }
                },
                "tag": "纸玫瑰",
                "mark_id": null,
                "timestamp": 1441731254
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "3808",
                    "nickname": "金雅",
                    "avatar": "http://cdn.likeorz.com/avatar_3808_1437064114.jpg?imageView2/5/w/80",
                    "likes": 262
                },
                "posts": [
                    {
                        "post_id": 76355,
                        "content": "http://cdn.likeorz.com/373828ad9320499bb9ac89a3f447fbbd_1441710493_w_826_h_826_6692.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "6692",
                            "nickname": "拆盒网岑岑",
                            "avatar": "http://cdn.likeorz.com/avatar_6692_1441725536.jpg?imageView2/5/w/80",
                            "likes": 46
                        }
                    }
                ],
                "post": {
                    "post_id": 76355,
                    "content": "http://cdn.likeorz.com/373828ad9320499bb9ac89a3f447fbbd_1441710493_w_826_h_826_6692.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "6692",
                        "nickname": "拆盒网岑岑",
                        "avatar": "http://cdn.likeorz.com/avatar_6692_1441725536.jpg?imageView2/5/w/80",
                        "likes": 46
                    }
                },
                "tag": "红玫瑰",
                "mark_id": null,
                "timestamp": 1441731254
            }
        ],
        "ts": 1441731254
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