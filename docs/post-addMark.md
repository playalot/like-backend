##Post Mark
|--------|-------|-------|
|LIKE-SESSION-TOKEN|true|string|Header中带用户授权|
|{id:[0-9]+}|true|int|作品ID|
|tag|true|string|标签内容|

    "code": "1", 
    "message": "Mark Success", 
    "data": {
        "mark_id": "8008", 
        "tag": "蛇精病", 
        "likes": 1, 
        "is_liked": "1"
    }
}
</pre>
|--------|-------|-------|
|mark_id|int|标签ID|
|tag|string|标签名|
|likes|int|点赞数|
|is_liked|int|是否赞过|