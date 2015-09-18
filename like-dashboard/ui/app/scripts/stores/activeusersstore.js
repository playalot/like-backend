var Reflux = require('reflux');
var $ = require('jquery');

var ActiveUsersStore = Reflux.createStore({
    data: {
      userlist: [],
      total: 0
    },
    init: function() {
      this.onFetchActiveUsers();
    },
    getInitialState: function() {
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
