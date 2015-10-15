var Reflux = require('reflux');
var $ = require('jquery');
var _ = require('lodash');
var BanUserActions = require('../actions/banuseractions');

var BanUserStore = Reflux.createStore({
    listenables: [BanUserActions],

    init: function() {
      this.userlist = [];
    },
    getInitialState: function() {
  		if (this.userlist.length === 0) {
        this.onFetchBannedUsers();
      }
      return this.userlist;
  	},
    onFetchBannedUsers: function() {
      $.ajax({
          url: '/api/users/banned',
          dataType: 'json',
          context: this,
          success: function(data) {
              console.log('fetch banbed users complete');
              this.userlist = data.users;
              this.trigger(this.userlist);
          }
      });
    },
    onBanUser: function(id) {
      $.ajax({
          url: '/api/user/'+id+'/ban',
          type: 'POST',
          context: this,
          success: function(data) {
            this.userlist.push(data);
            this.trigger(this.userlist);
          }
      });
    },
    onRemoveBanUser: function(id) {
      $.ajax({
          url: '/api/user/'+id+'/ban',
          type: 'DELETE',
          context: this,
          success: function(data) {
              this.userlist = _.filter(this.userlist, function(user){
                return user.id !== id;
              });
              this.trigger(this.userlist);
          }
      });
    }
});

module.exports = BanUserStore;
