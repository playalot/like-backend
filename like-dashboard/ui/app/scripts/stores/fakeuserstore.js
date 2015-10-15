var Reflux = require('reflux');
var FakeUserActions = require('../actions/fakeuseractions');
var Cookie = require('react-cookie');
var _ = require('lodash');

var FakeUserStore = Reflux.createStore({
    listenables: [FakeUserActions],

    init: function() {
      this.fake = {fakeuser: null, fakeusers: []};
    },
    getInitialState: function() {
      if (!this.fake.fakeuser) {
        this.onLoadFakeUser();
      }
      return this.fake;
    },
    onLoadFakeUser: function() {
      $.get('/api/admin/fakeusers', function(data){
        var fakeuserId = Cookie.load('fakeuserId');
        if (fakeuserId) {
          this.fake.fakeuser = _.find(data, function(fk) {
             return fk.user_id === fakeuserId;
          });
        }
        if (this.fake.fakeuser === null) {
          this.fake.fakeuser = data[0];
          Cookie.save('fakeuserId', this.fake.fakeuser.user_id);
        }
        this.fake.fakeusers = data;
        this.trigger(this.fake);
      }.bind(this));
    },
    onSelectFakeUser: function(fk) {
      Cookie.save('fakeuserId', fk.user_id);
      this.fake.fakeuser = fk;
      this.trigger(this.fake);
    },
    onCreateFakeUser: function() {
      $.post('/api/admin/fakeuser/add', function(fk){
        this.fake.fakeusers.push(fk);
        this.trigger(this.fake);
      }.bind(this));
    },
});

module.exports = FakeUserStore;
