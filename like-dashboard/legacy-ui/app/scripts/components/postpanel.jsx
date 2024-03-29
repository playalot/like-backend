var React = require('react');
var Col = require('react-bootstrap').Col;
var Tooltip = require('react-bootstrap').Tooltip;
var ButtonToolbar = require('react-bootstrap').ButtonToolbar;
var OverlayTrigger = require('react-bootstrap').OverlayTrigger;
var PostActions = require('../actions/postactions');

var PostPanel = React.createClass({
  propTypes: {
    post: React.PropTypes.object,
    open: React.PropTypes.func,
    showUser: React.PropTypes.bool
  },
  getDefaultProps: function() {
    return { showUser: true };
  },
  getInitialState: function() {
    return { showMarks: true };
  },
  deletePost: function() {
    if (confirm('Delete this post?')) {
      PostActions.deletePost(this.props.post.id);
    }
  },
  toggleRecommendPost: function() {
    if (confirm('Recommend this post?')) {
      PostActions.toggleRecommendPost(this.props.post.id);
    }
  },
  toggleBlockPost: function() {
    if (confirm('Make this post invisible to users?')) {
      PostActions.toggleBlockPost(this.props.post.id);
    }
  },
  deleteMark: function(mid) {
    if (confirm('Delete this mark?')) {
      PostActions.deleteMark(this.props.post.id, mid);
    }
  },
  toggleShowMarks: function() {
    this.setState({ showMarks: !this.state.showMarks});
  },
  nothing: function() {
    console.log('nothing happened');
  },
  render: function() {
    var marks = this.props.post.marks.map(function (mark) {
      return (
        <li key={'p_'+this.props.post.id+'_m_'+mark.markId} className="post-mark-li" onClick={ this.deleteMark.bind(this, mark.markId) } >{mark.tag}</li>
      );
    }.bind(this));

    var markPanel = '';
    if (this.state.showMarks === true) {
      markPanel = (<div className="thumb-pane">
          <ul className="marks">{ marks }</ul>
      </div>)
    }

    var recommendClass = 'post-caption-btn btn btn-default btn-sm';
    if (this.props.post.isRecommended === true) {
      recommendClass = 'post-caption-btn btn btn-warning btn-sm';
    }
    var invisibleClass = 'post-caption-btn btn btn-default btn-sm';
    if (this.props.post.isBlocked === true) {
      invisibleClass = 'post-caption-btn btn btn-warning btn-sm';
    }
    var tooltip = <Tooltip>{'id:'+this.props.post.id}</Tooltip>;
      var panelHeading = null;
      if (this.props.showUser === true) {
        panelHeading = (<div className="panel-heading post-heading">
          <a href={ '/user/' + this.props.post.user.userId } className="media">
            <span className="pull-left media-object thumb thumb-sm">
              <img img-src={'http://cdn.likeorz.com'} src={ this.props.post.user.avatar } alt="" className="img-circle" />
            </span>
            <div className="pull-left post-user-info">
              <span className="block">{ this.props.post.user.nickname }</span>
              <small className="text-muted">{ this.props.post.user.likes }</small>
            </div>
          </a>
        </div>);
      }
      return (
        <Col className="col" xs={12} sm={3} lg={3}>
          <div className="panel post-panel panel-default">
            {panelHeading}
            <div className="post-image">
              <img src={ this.props.post.content } onClick={this.props.open}/>
              { markPanel }
              <div className="post-caption">
                <ButtonToolbar className="pull-left">
                  <OverlayTrigger overlay={tooltip} placement='top' delayShow={100} delayHide={100}>
                    <span onClick={ this.toggleShowMarks } className="post-caption-btn btn btn-default btn-sm"><span className="glyphicon glyphicon-info-sign"></span></span>
                  </OverlayTrigger>
                </ButtonToolbar>
                <ButtonToolbar className="pull-right">
                  <span onClick={ this.toggleBlockPost } className={invisibleClass}><span className="glyphicon glyphicon glyphicon-eye-close"></span></span>
                  <span onClick={ this.toggleRecommendPost } className={recommendClass}><span className="glyphicon glyphicon-bullhorn"></span></span>
                  <span onClick={ this.deletePost } className="post-caption-btn btn btn-danger btn-sm"><span className="glyphicon glyphicon-trash"></span></span>
                </ButtonToolbar>
                <div className="clearfix"></div>
              </div>
            </div>
          </div>
        </Col>
      );
    }
  });

  module.exports = PostPanel;
