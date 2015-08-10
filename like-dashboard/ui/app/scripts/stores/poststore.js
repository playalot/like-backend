var Reflux = require('reflux');
var $ = require('jquery');
var _ = require('lodash');
var PostActions = require('../actions/postactions');

var PostStore = Reflux.createStore({
    listenables: [PostActions],
    postlist: [],
    timestamp: null,

    init: function() {
      this.onFetchPostList();
    },
    getInitialState: function() {
      return this.postlist;
    },
    onFetchPostList: function() {
      var sourceUrl = '/api/posts';
      if (this.timestamp !== null) {
        sourceUrl = sourceUrl + '?ts=' + this.timestamp;
      }
      $.ajax({
          url: sourceUrl,
          dataType: 'json',
          context: this,
          success: function(data) {
              console.log('fetch complete');
              this.postlist = this.postlist.concat(data.posts);
              this.timestamp = data.nextTimestamp;
              this.trigger(this.postlist);
          }
      });
    },
    onDeletePost: function(id) {
      $.ajax({
        url: '/api/post/'+id,
        type: 'DELETE',
        success: function() {
          console.log('delete post ' + id);
          this.updateList(_.filter(this.postlist, function(post){
            return post.id !== id;
          }));
        }.bind(this)
      });
    },
    onToggleRecommendPost: function(id) {
      var foundPost = _.find(this.postlist, function(post) {
          return post.id === id;
      });
      if (foundPost) {
        var type = 'POST';
        if (foundPost.isRecommended === true) {
          type = 'DELETE';
        }
        $.ajax({
          url: '/api/post/'+id+'/recommend',
          type: type,
          success: function() {
            console.log('toggle recommend post ' + id);
            foundPost.isRecommended = !foundPost.isRecommended;
            this.updateList(this.postlist);
          }.bind(this)
        });
      }
    },
    onToggleBlockPost: function(id) {
      var foundPost = _.find(this.postlist, function(post) {
            return post.id === id;
      });
      if (foundPost) {
        var type = 'POST';
        if (foundPost.isBlocked === true) {
          type = 'DELETE';
        }
        $.ajax({
          url: '/api/post/'+id+'/block',
          type: type,
          success: function() {
            console.log('toggle block post ' + id);
            foundPost.isBlocked = !foundPost.isBlocked;
            this.updateList(this.postlist);
          }.bind(this)
        });
      }
    },
    onDeleteMark: function(pid, mid) {
      var foundPost = _.find(this.postlist, function(post) {
          return post.id === pid;
      });
      if (foundPost) {
        $.ajax({
          url: '/api/mark/'+mid,
          type: 'DELETE',
          success: function() {
            console.log('delete mark ' + mid);
            foundPost.marks = _.filter(foundPost.marks, function(mark){
              return mark.markId !== mid;
            });
            this.updateList(this.postlist);
          }.bind(this)
        });
      }
    },
    updateList: function(list){
      this.postlist = list;
      this.trigger(this.postlist);
    }
});

module.exports = PostStore;
