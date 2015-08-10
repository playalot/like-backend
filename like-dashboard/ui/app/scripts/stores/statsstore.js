var Reflux = require('reflux');
var $ = require('jquery');

var StatsStore = Reflux.createStore({
    stats: {},
    init: function() {
      this.onUpdateStats();
    },
    getInitialState: function() {
      return this.stats;
    },
    onUpdateStats: function() {
      $.get('/api/stats', function(data){
        this.stats = data;
        this.trigger(this.stats);
      }.bind(this));
    }
});

setInterval(function() {
    StatsStore.onUpdateStats();
}, 5000);

module.exports = StatsStore;
