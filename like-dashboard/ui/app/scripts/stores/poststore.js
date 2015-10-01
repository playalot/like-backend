var Reflux = require('reflux');
var $ = require('jquery');
var _ = require('lodash');
var PostActions = require('../actions/postactions');

var PostListStore = Reflux.createStore({
    listenables: [PostActions],

    init: function() {
      this.postlist = [];
      this.filter = '';
      this.timestamp = '';
    },
    getInitialState: function() {
      if (this.postlist.length === 0) {
        this.onFetchPostList();
      }
      return this.postlist;
    },
    updateParams: function(v) {
      if (this.filter !== v) {
          this.timestamp = '';
          this.postlist = [];
      }
      this.filter = v;
    },
    onFetchPostList: function() {
      var sourceUrl = '/api/posts';
      if (this.timestamp !== '' && this.filter !== '') {
        sourceUrl = sourceUrl + '?ts=' + this.timestamp + '&filter=' + this.filter;
      } else if (this.timestamp !== '') {
        sourceUrl = sourceUrl + '?ts=' + this.timestamp;
      } else if (this.filter !== '') {
        sourceUrl = sourceUrl + '?filter=' + this.filter;
      }
      $.ajax({
          url: sourceUrl,
          dataType: 'json',
          context: this,
          success: function(data) {
              console.log('fetch posts complete');
              if (data.posts.length === 0) {
                alert('no more');
              } else {
                this.postlist = this.postlist.concat(data.posts);
                this.timestamp = data.nextTimestamp;
                this.trigger(this.postlist);
              }
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
      console.log(this.postlist);
      console.log(foundPost);
      if (foundPost) {
        console.log(foundPost);
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
    onAddMark: function(pid, tagName, uid) {
      var foundPost = _.find(this.postlist, function(post) {
          return post.id === pid;
      });
      if (foundPost) {
        $.ajax({
          url: '/api/post/'+pid+'/mark/'+tagName+'/'+uid,
          type: 'POST',
          success: function(data) {
            console.log('add mark ' + tagName);
            foundPost.marks.push(data);
            this.updateList(this.postlist);
          }.bind(this),
          error: function(data) {
            alert(data);
          }
        });
      }
    },
    updateList: function(list){
      this.postlist = list;
      this.trigger(this.postlist);
    }
});

module.exports = PostListStore;
