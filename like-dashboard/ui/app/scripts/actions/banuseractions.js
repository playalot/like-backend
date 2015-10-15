var Reflux = require('reflux');

var BanUserActions = Reflux.createActions([
    'fetchBannedUsers',
    'banUser',
    'removeBanUser'
]);

module.exports = BanUserActions;
