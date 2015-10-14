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
      user: null,
      postlist: []
    },
    getInitialState: function() {
      return this.userDetail;
    },
    onUpdateUserId: function(userId) {
      if (this.userDetail.userId !== userId) {
          this.userDetail.userId = userId;
          this.onFetchUserDetailInfo();
          this.userDetail.timestamp = '';
          this.userDetail.postlist = [];
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
              this.userDetail.user = data;
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
              this.userDetail.postlist = this.userDetail.postlist.concat(data.posts);
              this.userDetail.timestamp = data.next;
              this.trigger(this.userDetail);
            }
          }
      });
    },
    onDestroy: function() {
      $.ajax({
          url: '/api/user/'+this.userDetail.userId+'/register',
          type: 'DELETE',
          success: function(data) {
            location.reload();
          },
          error: function() {
            alert('error!');
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
          this.updateList(_.filter(this.userDetail.postlist, function(post){
            return post.id !== id;
          }));
        }.bind(this)
      });
    },
    onToggleRecommendPost: function(id) {
      var foundPost = _.find(this.userDetail.postlist, function(post) {
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
            this.updateList(this.userDetail.postlist);
          }.bind(this)
        });
      }
    },
    onToggleBlockPost: function(id) {
      var foundPost = _.find(this.userDetail.postlist, function(post) {
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
            this.updateList(this.userDetail.postlist);
          }.bind(this)
        });
      }
    },
    onDeleteMark: function(pid, mid) {
      var foundPost = _.find(this.userDetail.postlist, function(post) {
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
            this.updateList(this.userDetail.postlist);
          }.bind(this)
        });
      }
    },
    onAddMark: function(pid, tagName, uid) {
      var foundPost = _.find(this.userDetail.postlist, function(post) {
          return post.id === pid;
      });
      if (foundPost) {
        $.ajax({
          url: '/api/post/'+pid+'/mark/'+tagName+'/'+uid,
          type: 'POST',
          success: function(data) {
            console.log('add mark ' + tagName);
            foundPost.marks.push(data);
            this.updateList(this.userDetail.postlist);
          }.bind(this),
          error: function(data) {
            alert(data);
          }
        });
      }
    },
    onLike: function(pid, mark, uid) {
      var foundPost = _.find(this.userDetail.postlist, function(post) {
          return post.id === pid;
      });
      if (foundPost) {
        $.ajax({
          url: '/api/mark/'+mark.markId+'/'+uid,
          type: 'POST',
          success: function(data) {
            console.log(mark.markId + ' like by ' + uid);
            var foundMark = _.find(foundPost.marks, function(m) {
                return m.markId === mark.markId;
            });
            if (foundMark) {
              foundMark.likedBy.push(uid);
            }
            this.updateList(this.userDetail.postlist);
          }.bind(this),
          error: function(data) {
            alert(data);
          }
        });
      }
    },
    onUnlike: function(pid, mark, uid) {
      var foundPost = _.find(this.userDetail.postlist, function(post) {
          return post.id === pid;
      });
      if (foundPost) {
        $.ajax({
          url: '/api/mark/'+mark.markId+'/'+uid,
          type: 'DELETE',
          success: function(data) {
            console.log(mark.markId + ' unlike by ' + uid);
            var foundMark = _.find(foundPost.marks, function(m) {
                return m.markId === mark.markId;
            });
            if (foundMark) {
              foundMark.likedBy = _.filter(foundMark.likedBy, function(l){
                return l !== uid;
              });
            }
            this.updateList(this.userDetail.postlist);
          }.bind(this),
          error: function(data) {
            alert(data);
          }
        });
      }
    },
    updateList: function(list){
      this.userDetail.postlist = list;
      this.trigger(this.userDetail);
    }
});

module.exports = UserDetailStore;
