var Reflux = require('reflux');
var React = require('react');
var Row = require('react-bootstrap').Row;
var Col = require('react-bootstrap').Col;
var Odometer = require('../widgets/odometer');
var StatsStore = require('../stores/statsstore');
var StatsActions = require('../actions/statsactions');

var Home = React.createClass({
  mixins: [Reflux.connect(StatsStore, 'stats')],
  render: function() {
    if (this.state.stats) {
      return (
      <div className="content">
        <div className="box">
          <div className="box-header"></div>
          <div className="box-body text-center">
            <p>We will change the world.</p>
            <p><em>Innovation distinguishes between a leader and a follower.</em></p>
            <small>
                — Steve Jobs
            </small>
            </div>
        </div>
        <Row>
          <div className="col-md-12">
            <div className="box">
              <div className="box-header"></div>
              <div className="box-footer">
              <Row>
                <div className="col-sm-3 col-xs-6">
                  <div className="description-block border-right">
                    <h5 className="description-header"><Odometer value={ this.state.stats.user } /></h5>
                    <span className="description-text">TOTAL USERS</span>
                  </div>
                </div>
                <div className="col-sm-3 col-xs-6">
                  <div className="description-block border-right">
                    <h5 className="description-header"><Odometer value={ this.state.stats.post } /></h5>
                    <span className="description-text">TOTAL POSTS</span>
                  </div>
                </div>
                <div className="col-sm-3 col-xs-6">
                  <div className="description-block border-right">
                    <h5 className="description-header"><Odometer value={ this.state.stats.mark } /></h5>
                    <span className="description-text">TOTAL MARKS</span>
                  </div>
                </div>
                <div className="col-sm-3 col-xs-6">
                  <div className="description-block">
                    <h5 className="description-header"><Odometer value={ this.state.stats.tag } /></h5>
                    <span className="description-text">TOTAL TAGS</span>
                  </div>
                </div>
              </Row>
              <Row>
                <div className="col-sm-3 col-xs-6">
                  <div className="description-block border-right">
                    <h5 className="description-header"><Odometer value={ this.state.stats.like } /></h5>
                    <span className="description-text">TOTAL LIKES</span>
                  </div>
                </div>
                <div className="col-sm-3 col-xs-6">
                  <div className="description-block border-right">
                    <h5 className="description-header"><Odometer value={ this.state.stats.follow } /></h5>
                    <span className="description-text">TOTAL FOLLOWS</span>
                  </div>
                </div>
                <div className="col-sm-3 col-xs-6">
                  <div className="description-block border-right">
                    <h5 className="description-header"><Odometer value={ this.state.stats.notify } /></h5>
                    <span className="description-text">TOTAL NOTIFICATIONS</span>
                  </div>
                </div>
                <div className="col-sm-3 col-xs-6">
                  <div className="description-block">
                    <h5 className="description-header"><Odometer value={ this.state.stats.comment } /></h5>
                    <span className="description-text">TOTAL COMMENTS</span>
                  </div>
                </div>
              </Row>
              </div>
            </div>
          </div>
        </Row>
      </div>
      );
    } else {
      return (<div/>);
    }
  }
});

module.exports = Home;
