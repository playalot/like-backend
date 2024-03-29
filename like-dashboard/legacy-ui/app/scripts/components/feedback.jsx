var React = require('react');
var Reflux = require('reflux');
var Row = require('react-bootstrap').Row;
var ButtonToolbar = require('react-bootstrap').ButtonToolbar;
var FeedbackStore = require('../stores/feedbackstore');
var FeedbackActions = require('../actions/feedbackactions');
var Moment = require('moment');

var Feedbacks = React.createClass({
  mixins: [Reflux.connect(FeedbackStore, 'feedbacklist')],
  fetchMoreFeedbacks: function() {
    FeedbackActions.fetchFeedbackList();
  },
  deleteFeedback: function(id) {
    if (confirm('Delete this Feedback?')) {
      FeedbackActions.deleteFeedback(id);
    }
  },
  render: function() {
    if (this.state.feedbacklist) {
      return (
        <div>
          <div className="table-responsive">
            <table className="table table-striped">
              <thead><tr><th>#ID</th><th>User</th><th>Feedback</th><th style={{'min-width': '150px'}}>Created</th><th>Action</th></tr></thead>
              <tbody>
                {this.state.feedbacklist.map(function (feedback) {
                  return (
                    <tr key={'u_'+feedback.id}>
                      <td>{feedback.id}</td>
                      <td><a href={'/user/'+feedback.user.id}><img src={feedback.user.avatar} className="img-circle"/></a></td>
                      <td>{feedback.content}</td>
                      <td>{Moment.unix(feedback.created).fromNow()}</td>
                      <td><ButtonToolbar><a className="btn btn-red btn-sm" onClick={this.deleteFeedback.bind(this, feedback.id)}>Delete</a></ButtonToolbar></td>
                    </tr>
                  );
                }.bind(this))}
                <tr></tr>
              </tbody>
            </table>
          </div>
          <Row>
            <div className="load-more-btn" onClick={this.fetchMoreFeedbacks}>Load More</div>
          </Row>
        </div>
      );
    } else {
      return (<Row></Row>);
    }
  }
});

module.exports = Feedbacks;
