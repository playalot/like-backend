var Reflux = require('reflux');

var UserDetailActions = Reflux.createActions([
    'fetchUserDetailInfo',
    'fetchUserPosts',
    'updateUserId',
    'destroy',
    'updateUserInfo',
    'refreshUserCount',
    'changeForm'
]);

module.exports = UserDetailActions;
