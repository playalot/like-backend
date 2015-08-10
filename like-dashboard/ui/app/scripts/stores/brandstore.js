var Reflux = require('reflux');
var $ = require('jquery');
var _ = require('lodash');
var BrandActions = require('../actions/brandactions');

var BrandStore = Reflux.createStore({
    listenables: [BrandActions],
    brandlist: [],
    page: 0,

    init: function() {
      this.onFetchBrandList();
    },
    getInitialState: function() {
      return this.brandlist;
    },
    onFetchBrandList: function() {
      var sourceUrl = '/api/brands/' + this.page;
      $.ajax({
          url: sourceUrl,
          dataType: 'json',
          context: this,
          success: function(data) {
              console.log('fetch complete');
              this.brandlist = this.brandlist.concat(data.brands);
              this.page = this.page + 1;
              if (data.brands.length === 0) {
                alert('no more');
              }
              this.trigger(this.brandlist);
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
