var Reflux = require('reflux');
var $ = require('jquery');
var _ = require('lodash');
var ReportActions = require('../actions/reportactions');

var ReportStore = Reflux.createStore({
    listenables: [ReportActions],

    init: function() {
      this.reportlist = [];
      this.page = 0;
    },
    getInitialState: function() {
      if (this.reportlist.length === 0) {
        this.onFetchReportList();
      }
      return this.reportlist;
    },
    onFetchReportList: function() {
      var sourceUrl = '/api/report/' + this.page;
      $.ajax({
          url: sourceUrl,
          dataType: 'json',
          context: this,
          success: function(data) {
              this.reportlist = this.reportlist.concat(data.reports);
              this.page = this.page + 1;
              if (data.reports.length === 0 && this.page > 0) {
                alert('no more');
              }
              this.trigger(this.reportlist);
          }
      });
    },
    onDeleteReport: function(id) {
      $.ajax({
        url: '/api/report/'+id,
        type: 'DELETE',
        success: function() {
          console.log('delete report ' + id);
          this.updateList(_.filter(this.reportlist, function(report){
            return report.id !== id;
          }));
        }.bind(this)
      });
    },
    onDeletePost: function(id) {
      $.ajax({
        url: '/api/post/'+id,
        type: 'DELETE',
        success: function() {
          console.log('delete post ' + id);
          this.updateList(_.filter(this.reportlist, function(report){
            return report.post.postId !== id;
          }));
        }.bind(this)
      });
    },
    onHidePost: function(id) {
      var found = _.find(this.reportlist, function(report) {
            return report.post.postId === id;
      });
      if (found) {
        var type = 'POST';
        if (found.post.hidden === true) {
          type = 'DELETE';
        }
        $.ajax({
          url: '/api/post/'+id+'/block',
          type: type,
          success: function() {
            console.log('toggle block post ' + id);
            found.post.hidden = !found.post.hidden;
            this.updateList(this.reportlist);
          }.bind(this)
        });
      }
    },
    updateList: function(list){
      this.reportlist = list;
      this.trigger(this.reportlist);
    }
});

module.exports = ReportStore;
