var Reflux = require('reflux');

var UserDetailActions = Reflux.createActions([
    'fetchUserDetailInfo',
    'fetchUserPosts',
    'updateUserId',
    'destroy',
    'refreshUserCount'
]);

module.exports = UserDetailActions;
