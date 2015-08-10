var Reflux = require('reflux');

var PostActions = Reflux.createActions([
    'fetchPostList',
    'deletePost',
    'toggleRecommendPost',
    'toggleBlockPost',
    'deleteMark'
]);

module.exports = PostActions;
