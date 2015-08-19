var Reflux = require('reflux');
var $ = require('jquery');
var UserActions = require('../actions/useractions');

var UserListStore = Reflux.createStore({
    listenables: [UserActions],
    userlist: [],
    page: 0,
    filter: '',

    init: function() {
      this.onFetchUserList();
    },
    getInitialState: function() {
      return this.userlist;
    },
    updateParams: function(v) {
      if (this.filter !== v) {
          this.page = 0;
          this.userlist = [];
      }
      this.filter = v;
    },
    onFetchUserList: function() {
      var sourceUrl = '/api/users/' + this.page;
      if (this.filter.length > 0) {
        sourceUrl = sourceUrl + '?filter=' + this.filter;
      }
      $.ajax({
          url: sourceUrl,
          dataType: 'json',
          context: this,
          success: function(data) {
              console.log('fetch users complete');
              this.userlist = this.userlist.concat(data.users);
              this.page = this.page + 1;
              if (data.users.length === 0) {
                alert('no more');
              }
              this.trigger(this.userlist);
          }
      });
    }
});

module.exports = UserListStore;
