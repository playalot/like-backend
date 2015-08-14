var Reflux = require('reflux');
var $ = require('jquery');
var _ = require('lodash');
var UserActions = require('../actions/useractions');

var BrandStore = Reflux.createStore({
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
    },
    onDeleteBrand: function(id) {
      $.ajax({
        url: '/api/brand/'+id,
        type: 'DELETE',
        success: function() {
          console.log('delete brand ' + id);
          this.updateList(_.filter(this.brandlist, function(brand){
            return brand.id !== id;
          }));
        }.bind(this)
      });
    },
    onTogglePromoteBrand: function(id) {
      var foundBrand = _.find(this.brandlist, function(brand) {
          return brand.id === id;
      });
      if (foundBrand) {
        var type = 'POST';
        if (foundBrand.isPromoted === true) {
          type = 'DELETE';
        }
        $.ajax({
          url: '/api/brand/'+id+'/promote',
          type: type,
          success: function() {
            console.log('toggle promote brand ' + id);
            foundBrand.isPromoted = !foundBrand.isPromoted;
            this.updateList(this.brandlist);
          }.bind(this)
        });
      }
    },
    updateList: function(list){
      this.brandlist = list;
      this.trigger(this.brandlist);
    }
});

module.exports = BrandStore;
