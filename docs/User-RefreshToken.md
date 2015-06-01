##User Refresh Token
|--------|-------|-------|
|user/{id:[0-9]+}|true|int|用户ID|
|refresh_token|true|string|前一次获得的refresh_token|

    "code": "1", 
    "message": "Session Refresh Success", 
    "data": {
        "user_id": "1", 
        "session_token": "c5509917fcc2b870e5b4eabd4de7cd39", 
        "refresh_token": "Vp0qP5DBuaqQkCiW", 
        "expires_in": 604800
    }
}
</pre>
|--------|-------|-------|
|user_id|int|用户ID|
|session_token|string|新的用户授权|
|refresh_token|string|新的刷新授权|
|expires_in|int|过期时间|