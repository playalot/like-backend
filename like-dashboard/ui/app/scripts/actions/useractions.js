var Reflux = require('reflux');

var UserActions = Reflux.createActions([
    'updateParams',
    'fetchUserList',
    'toggleVerifyUser'
]);

module.exports = UserActions;
