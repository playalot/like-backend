var $ = require('jquery');
var React = require('react');

var Home = React.createClass({
  getInitialState: function() {
    return {
      stats: {
        user: 0,
        post: 0,
        mark: 0,
        tag: 0,
        like: 0,
        follow: 0,
        notify: 0
      }
    };
  },
  componentDidMount:function() {
    $.get('/api/stats', function(data){
      if (this.isMounted()) {
        this.setState({ stats: data });
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
                <h3 className="text-muted">{ this.state.stats.user }</h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Post</h5>
                <h3 className="text-muted">{ this.state.stats.post }</h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Mark</h5>
                <h3 className="text-muted">{ this.state.stats.mark }</h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Tag</h5>
                <h3 className="text-muted">{ this.state.stats.tag }</h3>
            </div>
        </div>
        <div className="row text-center">
            <div className="col-xs-6 col-sm-3">
                <h5>Like</h5>
                <h3 className="text-muted">{ this.state.stats.like }</h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Follow</h5>
                <h3 className="text-muted">{ this.state.stats.follow }</h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Notification</h5>
                <h3 className="text-muted">{ this.state.stats.notify }</h3>
            </div>
            <div className="col-xs-6 col-sm-3">
                <h5>Comment</h5>
                <h3 className="text-muted">{ this.state.stats.comment }</h3>
            </div>
        </div>
    </div>
    );
  }
});

module.exports = Home;
