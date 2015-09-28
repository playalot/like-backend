var Reflux = require('reflux');

var FeedbackActions = Reflux.createActions([
    'fetchFeedbackList',
    'deleteFeedback'
]);

module.exports = FeedbackActions;
