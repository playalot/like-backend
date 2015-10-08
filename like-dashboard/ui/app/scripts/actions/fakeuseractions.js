var Reflux = require('reflux');

var FakeUserActions = Reflux.createActions([
    'loadFakeUser',
    'selectFakeUser'
]);

module.exports = FakeUserActions;
