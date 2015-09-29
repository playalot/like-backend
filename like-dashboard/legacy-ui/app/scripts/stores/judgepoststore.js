var Reflux = require('reflux');
var $ = require('jquery');
var _ = require('lodash');
var JudgePostActions = require('../actions/judgepostactions');

var JudgePostListStore = Reflux.createStore({
    listenables: [JudgePostActions],

    init: function() {
      this.postlist = [];
      this.start = null;
    },
    getInitialState: function() {
      if (this.start === null) {
        this.onFetchPostList();
      }
      return this.postlist;
    },
    onFetchPostList: function() {
      var sourceUrl = '/api/unjudgedposts';
      if (this.start !== null) {
        sourceUrl = sourceUrl + '?start=' + this.start;
      }
      $.ajax({
          url: sourceUrl,
          dataType: 'json',
          context: this,
          success: function(data) {
              if (data.length === 0) {
                alert('no more');
              } else {
                this.postlist = this.postlist.concat(data);
                this.start = data[data.length -1].id;
                this.trigger(this.postlist);
              }
          }
      });
    },
    onJudge: function(id, judge) {
      $.ajax({
        url: '/api/judge/'+id+'/'+judge,
        type: 'POST',
        success: function() {
          this.updateList(_.filter(this.postlist, function(post){
            return post.id !== id;
          }));
        }.bind(this)
      });
    },
    updateList: function(list){
      this.postlist = list;
      this.trigger(this.postlist);
    }
});

module.exports = JudgePostListStore;
