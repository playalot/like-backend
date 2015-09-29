var Reflux = require('reflux');

var UserDetailActions = Reflux.createActions([
    'fetchUserDetailInfo',
    'fetchUserPosts',
    'updateUserId'
]);

module.exports = UserDetailActions;
