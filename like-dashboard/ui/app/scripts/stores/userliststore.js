var Reflux = require('reflux');
var $ = require('jquery');
var UserActions = require('../actions/useractions');

var UserListStore = Reflux.createStore({
    listenables: [UserActions],

    init: function() {
      this.userlist = [];
      this.page = 0;
      this.filter = '';
    },
    getInitialState: function() {
  		if (this.userlist.length === 0) {
        this.onFetchUserList();
      }
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
