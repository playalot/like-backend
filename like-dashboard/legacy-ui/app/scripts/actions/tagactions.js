var Reflux = require('reflux');

var TagActions = Reflux.createActions([
    'fetchTagGroups',
    'fetchTags',
    'setTagGroup',
    'addTagGroup',
    'unSetTagGroup'
]);

module.exports = TagActions;
