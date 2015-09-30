var Reflux = require('reflux');
var $ = require('jquery');
var Router = require('react-router');
var StatsActions = require('../actions/statsactions');

var StatsStore = Reflux.createStore({
    listenables: [StatsActions],

    init: function() {
      this.stats = {};
      this.sound = new Audio('/tick.wav');
    },
    getInitialState: function() {
      if (!this.stats.user) {
        this.onUpdateStats();
      }
      return this.stats;
    },
    repeatSound: function(times) {
      if (times > 0) {
        this.sound.play();
        setTimeout(function() {
          this.repeatSound(times-1);
        }.bind(this), 100);
      }
    },
    onUpdateStats: function() {
      if (window.location.hash === '#/' || window.location.hash === '#/home') {
        $.get('/api/stats', function(data){
          var count = 0;
          if (this.stats.user) {
            count = data.user - this.stats.user;
          }
          this.stats = data;
          this.repeatSound(count);
          this.trigger(this.stats);
        }.bind(this));
      }
    }
});

setInterval(function() {
    StatsStore.onUpdateStats();
}, 5000);

module.exports = StatsStore;
