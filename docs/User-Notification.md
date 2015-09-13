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
                "type": "LIKE",
                "user": {
                    "user_id": "9899",
                    "nickname": "༺࿅࿆࿄࿆ A-Puī ࿄࿆࿅࿆༻",
                    "avatar": "http://cdn.likeorz.com/avatar_9899_1439310398.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 9676,
                        "content": "http://cdn.likeorz.com/b041cb0767ed4343b008a8b2dc8d9919_1435334553_w_640_h_6223_1660.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "1660",
                            "nickname": "中尉",
                            "avatar": "http://cdn.likeorz.com/avatar_1660_1435156639.jpg?imageView2/5/w/80",
                            "likes": 0
                        }
                    }
                ],
                "post": {
                    "post_id": 9676,
                    "content": "http://cdn.likeorz.com/b041cb0767ed4343b008a8b2dc8d9919_1435334553_w_640_h_6223_1660.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "1660",
                        "nickname": "中尉",
                        "avatar": "http://cdn.likeorz.com/avatar_1660_1435156639.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tag": "卧槽你要去拯救世界吗",
                "mark_id": null,
                "timestamp": 1439318203
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "1263",
                    "nickname": "青蛙",
                    "avatar": "http://cdn.likeorz.com/avatar_1263_1439039277.jpg?imageView2/5/w/80",
                    "likes": 4711
                },
                "posts": [
                    {
                        "post_id": 9654,
                        "content": "http://cdn.likeorz.com/86d21d5e567d478eabb16ff00fb30703_1435326634_w_1280_h_1707_1757.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "1757",
                            "nickname": "不安的心灵",
                            "avatar": "http://cdn.likeorz.com/avatar_1757_1435318592.jpg?imageView2/5/w/80",
                            "likes": 0
                        }
                    }
                ],
                "post": {
                    "post_id": 9654,
                    "content": "http://cdn.likeorz.com/86d21d5e567d478eabb16ff00fb30703_1435326634_w_1280_h_1707_1757.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "1757",
                        "nickname": "不安的心灵",
                        "avatar": "http://cdn.likeorz.com/avatar_1757_1435318592.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tag": "钢铁侠",
                "mark_id": null,
                "timestamp": 1439317565
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "7969",
                    "nickname": "宇智波斑",
                    "avatar": "http://cdn.likeorz.com/avatar_7969_1438601527.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 22835,
                        "content": "http://cdn.likeorz.com/cc0f4c37e75641cd944d8cc3139d2b97_1438239388_w_1280_h_855_6482.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "6482",
                            "nickname": "林小财",
                            "avatar": "http://cdn.likeorz.com/avatar_6482_1438239128.jpg?imageView2/5/w/80",
                            "likes": 362
                        }
                    }
                ],
                "post": {
                    "post_id": 22835,
                    "content": "http://cdn.likeorz.com/cc0f4c37e75641cd944d8cc3139d2b97_1438239388_w_1280_h_855_6482.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "6482",
                        "nickname": "林小财",
                        "avatar": "http://cdn.likeorz.com/avatar_6482_1438239128.jpg?imageView2/5/w/80",
                        "likes": 362
                    }
                },
                "tag": "大飙客",
                "mark_id": null,
                "timestamp": 1439288078
            },
            {
                "type": "MARK",
                "user": {
                    "user_id": "7415",
                    "nickname": "Sceptila",
                    "avatar": "http://cdn.likeorz.com/avatar_7415_1438422153.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 19911,
                        "content": "http://cdn.likeorz.com/1bcb7a25dadf462e863256852a7722a1_1437904404_w_1280_h_1707_187.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "187",
                            "nickname": "红果果",
                            "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                            "likes": 2513
                        }
                    }
                ],
                "post": {
                    "post_id": 19911,
                    "content": "http://cdn.likeorz.com/1bcb7a25dadf462e863256852a7722a1_1437904404_w_1280_h_1707_187.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "187",
                        "nickname": "红果果",
                        "avatar": "http://cdn.likeorz.com/avatar_187_1438241120.jpg?imageView2/5/w/80",
                        "likes": 2513
                    }
                },
                "tag": "ef2000",
                "mark_id": null,
                "timestamp": 1439268709
            },
            {
                "type": "FOLLOW",
                "user": {
                    "user_id": "15",
                    "nickname": "松汤",
                    "avatar": "http://cdn.likeorz.com/avatar_15_1431375280.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                ],
                "timestamp": 1439141359
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "8395",
                    "nickname": "Jyo",
                    "avatar": "http://cdn.likeorz.com/avatar_8395_1438743883.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 10791,
                        "content": "http://cdn.likeorz.com/7a48d0a2af3b41539cb0020ccf2671be_1435828121_w_1280_h_1280_173.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "173",
                            "nickname": "akaaa",
                            "avatar": "http://cdn.likeorz.com/avatar_173_1432458009.jpg?imageView2/5/w/80",
                            "likes": 0
                        }
                    }
                ],
                "post": {
                    "post_id": 10791,
                    "content": "http://cdn.likeorz.com/7a48d0a2af3b41539cb0020ccf2671be_1435828121_w_1280_h_1280_173.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "173",
                        "nickname": "akaaa",
                        "avatar": "http://cdn.likeorz.com/avatar_173_1432458009.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tag": "训龙高手",
                "mark_id": null,
                "timestamp": 1439038870
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "8968",
                    "nickname": "Dou",
                    "avatar": "http://cdn.likeorz.com/avatar_8968_1438993819.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 10791,
                        "content": "http://cdn.likeorz.com/7a48d0a2af3b41539cb0020ccf2671be_1435828121_w_1280_h_1280_173.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "173",
                            "nickname": "akaaa",
                            "avatar": "http://cdn.likeorz.com/avatar_173_1432458009.jpg?imageView2/5/w/80",
                            "likes": 0
                        }
                    }
                ],
                "post": {
                    "post_id": 10791,
                    "content": "http://cdn.likeorz.com/7a48d0a2af3b41539cb0020ccf2671be_1435828121_w_1280_h_1280_173.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "173",
                        "nickname": "akaaa",
                        "avatar": "http://cdn.likeorz.com/avatar_173_1432458009.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tag": "训龙高手",
                "mark_id": null,
                "timestamp": 1439028178
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "8884",
                    "nickname": "Good_Smile",
                    "avatar": "http://cdn.likeorz.com/avatar_8884_1438952355.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 8696,
                        "content": "http://cdn.likeorz.com/ea6c2813955d4b648977660b8663352d_1434970898_w_1280_h_1707_1263.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "1263",
                            "nickname": "青蛙",
                            "avatar": "http://cdn.likeorz.com/avatar_1263_1439039277.jpg?imageView2/5/w/80",
                            "likes": 4711
                        }
                    }
                ],
                "post": {
                    "post_id": 8696,
                    "content": "http://cdn.likeorz.com/ea6c2813955d4b648977660b8663352d_1434970898_w_1280_h_1707_1263.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "1263",
                        "nickname": "青蛙",
                        "avatar": "http://cdn.likeorz.com/avatar_1263_1439039277.jpg?imageView2/5/w/80",
                        "likes": 4711
                    }
                },
                "tag": "恶魔果实",
                "mark_id": null,
                "timestamp": 1439022110
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "4181",
                    "nickname": "Kasper喵",
                    "avatar": "http://cdn.likeorz.com/avatar_4181_1437379383.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 6093,
                        "content": "http://cdn.likeorz.com/b1721a94a31a45d792b8f983345bbfff_1432871496_w_500_h_501_103.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "103",
                            "nickname": "LiKE果布斯的帅贱的保镖王小美",
                            "avatar": "http://cdn.likeorz.com/avatar_103_1436928485.jpg?imageView2/5/w/80",
                            "likes": 0
                        }
                    }
                ],
                "post": {
                    "post_id": 6093,
                    "content": "http://cdn.likeorz.com/b1721a94a31a45d792b8f983345bbfff_1432871496_w_500_h_501_103.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "103",
                        "nickname": "LiKE果布斯的帅贱的保镖王小美",
                        "avatar": "http://cdn.likeorz.com/avatar_103_1436928485.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tag": "女朋友们",
                "mark_id": null,
                "timestamp": 1439013424
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "8921",
                    "nickname": "毛线打纯爷们",
                    "avatar": "http://cdn.likeorz.com/avatar_8921_1438962881.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 15053,
                        "content": "http://cdn.likeorz.com/5bb99935c6fc461c8bf5a8b1799403f6_1436775308_w_648_h_803_2175.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "2175",
                            "nickname": "慕姥",
                            "avatar": "http://cdn.likeorz.com/avatar_2175_1438706691.jpg?imageView2/5/w/80",
                            "likes": 16633
                        }
                    }
                ],
                "post": {
                    "post_id": 15053,
                    "content": "http://cdn.likeorz.com/5bb99935c6fc461c8bf5a8b1799403f6_1436775308_w_648_h_803_2175.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "2175",
                        "nickname": "慕姥",
                        "avatar": "http://cdn.likeorz.com/avatar_2175_1438706691.jpg?imageView2/5/w/80",
                        "likes": 16633
                    }
                },
                "tag": "好多索隆",
                "mark_id": null,
                "timestamp": 1438965249
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "8714",
                    "nickname": "GLQ.",
                    "avatar": "http://cdn.likeorz.com/avatar_8714_1438956609.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 23091,
                        "content": "http://cdn.likeorz.com/f7dc0021c1c24651a8a0ad03ab6b7468_1438254498_w_1280_h_1918_6617.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "6617",
                            "nickname": "dashtwo",
                            "avatar": "http://cdn.likeorz.com/default_avatar.jpg?imageView2/5/w/80",
                            "likes": 0
                        }
                    }
                ],
                "post": {
                    "post_id": 23091,
                    "content": "http://cdn.likeorz.com/f7dc0021c1c24651a8a0ad03ab6b7468_1438254498_w_1280_h_1918_6617.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "6617",
                        "nickname": "dashtwo",
                        "avatar": "http://cdn.likeorz.com/default_avatar.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tag": "帅",
                "mark_id": null,
                "timestamp": 1438959008
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "8656",
                    "nickname": "shaco干部",
                    "avatar": "http://cdn.likeorz.com/avatar_8656_1438847665.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 20314,
                        "content": "http://cdn.likeorz.com/8fca7869f8d44b70a94fac6c70983ca6_1437972681_w_600_h_337_964.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "964",
                            "nickname": "FYS+_+",
                            "avatar": "http://cdn.likeorz.com/avatar_964_1439192767.jpg?imageView2/5/w/80",
                            "likes": 0
                        }
                    }
                ],
                "post": {
                    "post_id": 20314,
                    "content": "http://cdn.likeorz.com/8fca7869f8d44b70a94fac6c70983ca6_1437972681_w_600_h_337_964.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "964",
                        "nickname": "FYS+_+",
                        "avatar": "http://cdn.likeorz.com/avatar_964_1439192767.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tag": "舔",
                "mark_id": null,
                "timestamp": 1438937892
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "3504",
                    "nickname": "叫我七宝-",
                    "avatar": "http://cdn.likeorz.com/avatar_3504_1436795779.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 10791,
                        "content": "http://cdn.likeorz.com/7a48d0a2af3b41539cb0020ccf2671be_1435828121_w_1280_h_1280_173.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "173",
                            "nickname": "akaaa",
                            "avatar": "http://cdn.likeorz.com/avatar_173_1432458009.jpg?imageView2/5/w/80",
                            "likes": 0
                        }
                    }
                ],
                "post": {
                    "post_id": 10791,
                    "content": "http://cdn.likeorz.com/7a48d0a2af3b41539cb0020ccf2671be_1435828121_w_1280_h_1280_173.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "173",
                        "nickname": "akaaa",
                        "avatar": "http://cdn.likeorz.com/avatar_173_1432458009.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tag": "训龙高手",
                "mark_id": null,
                "timestamp": 1438931147
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "4181",
                    "nickname": "Kasper喵",
                    "avatar": "http://cdn.likeorz.com/avatar_4181_1437379383.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 5371,
                        "content": "http://cdn.likeorz.com/e00a3d6cdfa34b3cb340d7990dd02109_1432046125_w_750_h_562_636.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "636",
                            "nickname": "C_A",
                            "avatar": "http://cdn.likeorz.com/avatar_636_1431692140.jpg?imageView2/5/w/80",
                            "likes": 0
                        }
                    }
                ],
                "post": {
                    "post_id": 5371,
                    "content": "http://cdn.likeorz.com/e00a3d6cdfa34b3cb340d7990dd02109_1432046125_w_750_h_562_636.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "636",
                        "nickname": "C_A",
                        "avatar": "http://cdn.likeorz.com/avatar_636_1431692140.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tag": "喵星人",
                "mark_id": null,
                "timestamp": 1438928469
            },
            {
                "type": "LIKE",
                "user": {
                    "user_id": "1162",
                    "nickname": "眯先生",
                    "avatar": "http://cdn.likeorz.com/avatar_1162_1434381726.jpg?imageView2/5/w/80",
                    "likes": 0
                },
                "posts": [
                    {
                        "post_id": 23101,
                        "content": "http://cdn.likeorz.com/362dd160e4874080b213b5e2487c07b0_1438255222_w_1280_h_1707_5872.jpg?imageView2/0/w/180/h/180/q/85",
                        "user": {
                            "user_id": "5872",
                            "nickname": "avoooo",
                            "avatar": "http://cdn.likeorz.com/avatar_5872_1438232046.jpg?imageView2/5/w/80",
                            "likes": 0
                        }
                    }
                ],
                "post": {
                    "post_id": 23101,
                    "content": "http://cdn.likeorz.com/362dd160e4874080b213b5e2487c07b0_1438255222_w_1280_h_1707_5872.jpg?imageView2/0/w/180/h/180/q/85",
                    "user": {
                        "user_id": "5872",
                        "nickname": "avoooo",
                        "avatar": "http://cdn.likeorz.com/avatar_5872_1438232046.jpg?imageView2/5/w/80",
                        "likes": 0
                    }
                },
                "tag": "高达",
                "mark_id": null,
                "timestamp": 1438865882
            }
        ],
        "ts": 1438865882
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