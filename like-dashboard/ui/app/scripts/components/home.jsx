var $ = require('jquery');
var React = require('react');
var Odometer = require('../widgets/odometer');

var SetIntervalMixin = {
  componentWillMount: function() {
    this.intervals = [];
  },
  setInterval: function() {
    this.intervals.push(setInterval.apply(null, arguments));
  },
  componentWillUnmount: function() {
    this.intervals.map(clearInterval);
  }
};

var Home = React.createClass({
  mixins: [SetIntervalMixin], // Use the mixin
  getInitialState: function() {
    return {
        user: 0,
        post: 0,
        mark: 0,
        tag: 0,
        like: 0,
        follow: 0,
        notify: 0,
        comment: 0
    };
  },
  componentDidMount: function() {
    $.get('/api/stats', function(data){
      if (this.isMounted()) {
        this.setState({
          user: data.user,
          post: data.post,
          mark: data.mark,
          tag: data.tag,
          like: data.like,
          follow: data.follow,
          notify: data.notify,
          comment: data.comment
        });
        this.setInterval(this.updateStats, 5000);
      }
    }.bind(this));
  },
  updateStats: function() {
    $.get('/api/stats', function(data){
      if (this.isMounted()) {
        this.setState({
          user: data.user,
          post: data.post,
          mark: data.mark,
          tag: data.tag,
          like: data.like,
          follow: data.follow,
          notify: data.notify
        });
      }
    }.bind(this));
  },
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
                <h3 className="text-muted"><Odometer value={ this.state.user } /></h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Post</h5>
                <h3 className="text-muted"><Odometer value={ this.state.post } /></h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Mark</h5>
                <h3 className="text-muted"><Odometer value={ this.state.mark } /></h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Tag</h5>
                <h3 className="text-muted"><Odometer value={ this.state.tag } /></h3>
            </div>
        </div>
        <div className="row text-center">
            <div className="col-xs-6 col-sm-3">
                <h5>Like</h5>
                <h3 className="text-muted"><Odometer value={ this.state.like } /></h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Follow</h5>
                <h3 className="text-muted"><Odometer value={ this.state.follow } /></h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Notification</h5>
                <h3 className="text-muted"><Odometer value={ this.state.notify } /></h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Comment</h5>
                <h3 className="text-muted"><Odometer value={ this.state.comment } /></h3>
            </div>
        </div>
    </div>
    );
  }
});

module.exports = Home;
