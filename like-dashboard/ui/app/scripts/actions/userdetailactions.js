var Reflux = require('reflux');

var UserDetailActions = Reflux.createActions([
    'fetchUserDetailInfo',
    'fetchUserPosts',
    'updateUserId',
    'destroy'
]);

module.exports = UserDetailActions;
