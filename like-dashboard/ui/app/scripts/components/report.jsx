var React = require('react');
var Reflux = require('reflux');
var Row = require('react-bootstrap').Row;
var Modal = require('react-bootstrap').Modal;
var ButtonToolbar = require('react-bootstrap').ButtonToolbar;
var ReportStore = require('../stores/reportstore');
var ReportActions = require('../actions/reportactions');
var Link = require('react-router').Link;
var Moment = require('moment');

var Reports = React.createClass({
  mixins: [Reflux.connect(ReportStore, 'reportlist')],
  getInitialState: function() {
    return { showModal: false, showPost: null };
  },
  fetchMoreReports: function() {
    ReportActions.fetchReportList();
  },
  deleteReport: function(id) {
    if (confirm('Delete this report?')) {
      ReportActions.deleteReport(id);
    }
  },
  deletePost: function(id) {
    if (confirm('Delete this post?')) {
      ReportActions.deletePost(id);
    }
  },
  hidePost: function(id) {
    if (confirm('Hide this post?')) {
      ReportActions.hidePost(id);
    }
  },
  close: function() {
    this.setState({ showModal: false });
  },
  open: function(post) {
    console.log(post);
    this.setState({ showModal: true, showPost: post });
  },
  render: function() {
    var modalBody = (<div></div>);
    if (this.state.showPost) {
      var marks = this.state.showPost.marks.map(function (mark) {
        return (
          <span key={'rm_'+mark.markId} className="label label-danger" style={{float: 'left', 'font-size':'100%', margin: '2px'}} >{mark.tag} </span>
        );
      });
      modalBody = (
        <div className="box">
          <div className="box-body">
          <div className="user-block">
            <img className="img-circle img-bordered-sm" src={this.state.showPost.user.avatar} alt="user image" />
            <span className="username">
              {this.state.showPost.user.nickname}
            </span>
            <span className="description">{this.state.showPost.user.likes}</span>
          </div>
          <div>
            <img className="image-modal" src={this.state.showPost.preview}/>
          </div>
          <p>
            {marks}
          </p>
          </div>
        </div>
      );
    }
    if (this.state.reportlist) {
      return (
        <div className="content">
          <div className="table-responsive">
            <table className="table table-striped">
              <thead><tr><th>#ID</th><th>Reporter</th><th>Post</th><th>Reason</th><th style={{'minWidth': '150px'}}>Created</th><th>Action</th></tr></thead>
              <tbody>
                {this.state.reportlist.map(function (report) {
                  var hideBtnClass = 'btn btn-default btn-flat';
                  if (report.post.hidden === true) {
                    hideBtnClass = 'btn btn-warning btn-flat';
                  }
                  return (
                    <tr key={'rp_'+report.id}>
                      <td>{report.id}</td>
                      <td><Link to={'/user/'+report.user.userId}><img src={report.user.avatar} className="img-circle"/></Link></td>
                      <td><img src={report.post.thumbnail} className="img-rounded" style={{maxHeight:'50px'}} onClick={this.open.bind(this, report.post)}/></td>
                      <td>{report.reason}</td>
                      <td>{Moment.unix(report.created).fromNow()}</td>
                      <td>
                        <ButtonToolbar>
                          <a className="btn btn-danger btn-flat" onClick={this.deletePost.bind(this, report.post.postId)}><i className="fa fa-trash"></i></a>
                          <a className={hideBtnClass} onClick={this.hidePost.bind(this, report.post.postId)}><i className="fa fa-eye-slash"></i></a>
                          <a className="btn btn-success btn-flat" onClick={this.deleteReport.bind(this, report.id)}><i className="fa fa-check"></i></a>
                        </ButtonToolbar>
                      </td>
                    </tr>
                  );
                }.bind(this))}
                <tr></tr>
              </tbody>
            </table>
          </div>
          <Row>
            <div className="load-more-btn" onClick={this.fetchMoreReports}>Load More</div>
          </Row>
          <div>
            <Modal show={this.state.showModal} onHide={this.close}>
              {modalBody}
            </Modal>
          </div>
        </div>
      );
    } else {
      return (<Row></Row>);
    }
  }
});

module.exports = Reports;
