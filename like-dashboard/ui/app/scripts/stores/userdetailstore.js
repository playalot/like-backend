var Reflux = require('reflux');
var $ = require('jquery');
var _ = require('lodash');
var UserDetailActions = require('../actions/userdetailactions');
var PostActions = require('../actions/postactions');

var UserDetailStore = Reflux.createStore({
    listenables: [UserDetailActions, PostActions],
    userDetail: {
      userId: 0,
      timestamp: '',
      userInfo: {count:{}},
      userPostlist: []
    },

    getInitialState: function() {
      return this.userDetail;
    },
    onUpdateUserId: function(userId) {
      if (this.userDetail.userId !== userId) {
          this.userDetail.userId = userId;
          this.userDetail.timestamp = '';
          this.onFetchUserDetailInfo();
          this.onFetchUserPosts();
      }
    },
    onFetchUserDetailInfo: function() {
      $.ajax({
          url: '/api/user/' + this.userDetail.userId + '/info',
          dataType: 'json',
          context: this,
          success: function(data) {
              console.log('fetch user detail info complete');
              this.userDetail.userInfo = data;
              this.trigger(this.userDetail);
          }
      });
    },
    onFetchUserPosts: function() {
      var sourceUrl = '/api/user/' + this.userDetail.userId + '/posts';
      if (this.userDetail.timestamp !== '') {
        sourceUrl = sourceUrl + '?ts=' + this.userDetail.timestamp;
      }
      $.ajax({
          url: sourceUrl,
          dataType: 'json',
          context: this,
          success: function(data) {
            if (data.posts.length === 0 && this.userDetail.timestamp !== '') {
              alert('no more');
            }  else {
              console.log('fetch user posts complete');
              this.userDetail.userPostlist = this.userDetail.userPostlist.concat(data.posts);
              this.userDetail.timestamp = data.nextTimestamp;
              this.trigger(this.userDetail);
            }
          }
      });
    },
    onToggleBan: function(id) {
      console.log('toggle ban:' + id);
    },
    onToggleVerify: function(id) {
      console.log('toggle verify:' + id);
    },
    onDeletePost: function(id) {
      $.ajax({
        url: '/api/post/'+id,
        type: 'DELETE',
        success: function() {
          console.log('delete post ' + id);
          this.updateList(_.filter(this.userDetail.userPostlist, function(post){
            return post.id !== id;
          }));
        }.bind(this)
      });
    },
    onToggleRecommendPost: function(id) {
      var foundPost = _.find(this.userDetail.userPostlist, function(post) {
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
            this.updateList(this.userDetail.userPostlist);
          }.bind(this)
        });
      }
    },
    onToggleBlockPost: function(id) {
      var foundPost = _.find(this.userDetail.userPostlist, function(post) {
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
            this.updateList(this.userDetail.userPostlist);
          }.bind(this)
        });
      }
    },
    onDeleteMark: function(pid, mid) {
      var foundPost = _.find(this.userDetail.userPostlist, function(post) {
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
            this.updateList(this.userDetail.userPostlist);
          }.bind(this)
        });
      }
    },
    onAddMark: function(pid, tagName, uid) {
      var foundPost = _.find(this.userDetail.userPostlist, function(post) {
          return post.id === pid;
      });
      if (foundPost) {
        $.ajax({
          url: '/api/post/'+pid+'/mark/'+tagName+'/'+uid,
          type: 'POST',
          success: function(data) {
            console.log('add mark ' + tagName);
            foundPost.marks.push(data);
            this.updateList(this.userDetail.userPostlist);
          }.bind(this),
          error: function(data) {
            alert(data);
          }
        });
      }
    },
    updateList: function(list){
      this.userDetail.userPostlist = list;
      this.trigger(this.userDetail);
    }
});

module.exports = UserDetailStore;
