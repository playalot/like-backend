var Reflux = require('reflux');
var $ = require('jquery');
var _ = require('lodash');
var FeedbackActions = require('../actions/feedbackactions');

var FeedbackStore = Reflux.createStore({
    listenables: [FeedbackActions],

    init: function() {
      this.feedbacklist = [];
      this.page = 0;
    },
    getInitialState: function() {
      if (this.feedbacklist.length === 0) {
        this.onFetchFeedbackList();
      }
      return this.feedbacklist;
    },
    onFetchFeedbackList: function() {
      var sourceUrl = '/api/feedback/' + this.page;
      $.ajax({
          url: sourceUrl,
          dataType: 'json',
          context: this,
          success: function(data) {
              this.feedbacklist = this.feedbacklist.concat(data.feedbacks);
              this.page = this.page + 1;
              if (data.feedbacks.length === 0 && this.page > 0) {
                alert('no more');
              }
              this.trigger(this.feedbacklist);
          }
      });
    },
    onDeleteFeedback: function(id) {
      $.ajax({
        url: '/api/feedback/'+id,
        type: 'DELETE',
        success: function() {
          console.log('delete Feedback ' + id);
          this.updateList(_.filter(this.feedbacklist, function(feedback){
            return feedback.id !== id;
          }));
        }.bind(this)
      });
    },
    updateList: function(list){
      this.feedbacklist = list;
      this.trigger(this.feedbacklist);
    }
});

module.exports = FeedbackStore;
