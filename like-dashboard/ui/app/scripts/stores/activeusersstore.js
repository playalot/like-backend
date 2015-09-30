var Reflux = require('reflux');
var $ = require('jquery');
var ActiveUsersAction = require('../actions/activeuseractions');

var ActiveUsersStore = Reflux.createStore({
    listenables: [ActiveUsersAction],

    init: function() {
      this.data = { userlist: [], total: 0 };
    },
    getInitialState: function() {
  		if (this.data.userlist.length === 0) {
        this.onFetchActiveUsers();
      }
      return this.data;
  	},
    onFetchActiveUsers: function() {
      $.ajax({
          url: '/api/users/active',
          dataType: 'json',
          context: this,
          success: function(data) {
              console.log('fetch active users complete');
              this.data.userlist = data.users;
              this.data.total = data.total;
              this.trigger(this.data);
          }
      });
    }
});

module.exports = ActiveUsersStore;
