var Reflux = require('reflux');
var $ = require('jquery');
var React = require('react');
var Odometer = require('../widgets/odometer');
var StatsStore = require('../stores/statsstore');

var Home = React.createClass({
  mixins: [Reflux.connect(StatsStore, 'stats')],
  render: function() {
    return (
      <div className="site-index">
        <div className="jumbotron">
            <h1>Like</h1>
            <p>We will change the world.</p>
            <p><em><small>Innovation distinguishes between a leader and a follower.</small></em></p>
            <footer>
                — Steve Jobs
            </footer>
        </div>
        <div className="row text-center">
            <div className="col-xs-6 col-sm-3">
                <h5>User</h5>
                <h3 className="text-muted"><Odometer value={ this.state.stats.user } /></h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Post</h5>
                <h3 className="text-muted"><Odometer value={ this.state.stats.post } /></h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Mark</h5>
                <h3 className="text-muted"><Odometer value={ this.state.stats.mark } /></h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Tag</h5>
                <h3 className="text-muted"><Odometer value={ this.state.stats.tag } /></h3>
            </div>
        </div>
        <div className="row text-center">
            <div className="col-xs-6 col-sm-3">
                <h5>Like</h5>
                <h3 className="text-muted"><Odometer value={ this.state.stats.like } /></h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Follow</h5>
                <h3 className="text-muted"><Odometer value={ this.state.stats.follow } /></h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Notification</h5>
                <h3 className="text-muted"><Odometer value={ this.state.stats.notify } /></h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Comment</h5>
                <h3 className="text-muted"><Odometer value={ this.state.stats.comment } /></h3>
            </div>
        </div>
    </div>
    );
  }
});

module.exports = Home;
