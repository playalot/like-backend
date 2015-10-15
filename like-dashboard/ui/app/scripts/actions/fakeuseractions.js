var Reflux = require('reflux');

var FakeUserActions = Reflux.createActions([
    'loadFakeUser',
    'selectFakeUser',
    'createFakeUser'
]);

module.exports = FakeUserActions;
