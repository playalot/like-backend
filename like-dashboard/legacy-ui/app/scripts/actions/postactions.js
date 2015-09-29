var Reflux = require('reflux');

var PostActions = Reflux.createActions([
    'updateParams',
    'fetchPostList',
    'deletePost',
    'toggleRecommendPost',
    'toggleBlockPost',
    'deleteMark'
]);

module.exports = PostActions;
