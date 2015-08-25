var Reflux = require('reflux');
var $ = require('jquery');
var _ = require('lodash');
var TagActions = require('../actions/tagactions');

var TagStore = Reflux.createStore({
    listenables: [TagActions],
    data: {
      groups: [],
      tags: [],
      page: 0,
    },
    init: function() {
      this.onFetchTagGroups();
      this.onFetchTags();
    },
    getInitialState: function() {
      return this.data;
    },
    onFetchTagGroups: function() {
      $.ajax({
          url: '/api/tag/groups',
          dataType: 'json',
          context: this,
          success: function(data) {
              this.data.groups = data;
              this.trigger(this.data);
          }
      });
    },
    onFetchTags: function() {
      var sourceUrl = '/api/tag/group/0?page=' + this.data.page;
      $.ajax({
          url: sourceUrl,
          dataType: 'json',
          context: this,
          success: function(data) {
              this.data.tags = this.data.tags.concat(data);
              this.data.page = this.data.page + 1;
              if (data.length === 0 && this.data.page > 0) {
                alert('no more');
              }
              this.trigger(this.data);
          }
      });
    },
    onSetTagGroup: function(tid, gid) {
      $.ajax({
        url: '/api/tag/'+tid+'/group/'+gid,
        type: 'POST',
        success: function() {
          var movedTag = _.find(this.data.tags, function(t) {
            return t.id === tid;
          });
          this.data.tags=_.filter(this.data.tags, function(tag) {
              return tag.id !== tid;
          });
          var group = _.find(this.data.groups, function(grp) {
              return grp.groupId === gid;
          });
          group.tags.push(movedTag);
          this.trigger(this.data);
        }.bind(this)
      });
    },
    onAddTagGroup: function(name) {
      $.ajax({
        url: '/api/tag/group/'+name,
        type: 'POST',
        success: function(data) {
          this.data.groups.push(data);
          this.trigger(this.data);
        }.bind(this)
      });
    },
    onUnSetTagGroup: function(tid, gid) {
      $.ajax({
        url: '/api/tag/'+tid+'/group/0',
        type: 'POST',
        success: function() {
          var group = _.find(this.data.groups, function(grp) {
              return grp.groupId === gid;
          });
          var movedTag = _.find(group.tags, function(tag) {
            return tag.id === tid;
          });
          group.tags = _.filter(group.tags, function(tag) {
            return tag.id !== tid;
          });
          this.data.tags.push(movedTag);
          this.trigger(this.data);
        }.bind(this)
      });
    }
});

module.exports = TagStore;
