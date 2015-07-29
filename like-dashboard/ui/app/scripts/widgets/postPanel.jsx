var React = require('react');
var Router = require('react-router');

var Link = Router.Link;

var PostPanel = React.createClass({
  getInitialState: function() {
    return {
      nickname: '',
      likes: 0,
      avatar: '/images/default_avatar.jpg',
      isRecommended: false,
      isInvisible: false
    };
  },
  componentDidMount: function() {
    var infos = {};
    $.when(
      $.get('/api/user/'+this.props.userId+'/info', function(data) {
        if (this.isMounted()) {
          infos.nickname = data.nickname;
          infos.likes = data.likes;
          infos.avatar = data.avatar;
        }
      }.bind(this)),
      $.get('/api/post/'+this.props.postId+'/recommend', function(data) {
        if (this.isMounted()) {
          infos.isRecommended = data.status;
        }
      }.bind(this)),
      $.get('/api/post/'+this.props.postId+'/invisible', function(data) {
        if (this.isMounted()) {
          infos.isInvisible = data.status;
        }
      }.bind(this))
    ).done(function (v1, v2, v3) {
      this.setState(infos);
    }.bind(this));
  },
  deletePost: function(id) {
    if (confirm('Delete this post?')) {
      $.ajax({
        url: '/api/post/'+id,
        type: 'DELETE',
        success: function(result) {
          console.log('delete post ' + id);
          React.findDOMNode(this.refs['post_'+id]).remove();
        }.bind(this)
      });
    }
  },
  toggleRecommentPost: function(id, status) {
    if (confirm('Recommend this post?')) {
      var type = 'POST';
      if (status === true) {
        type = 'DELETE';
      }
      $.ajax({
        url: '/api/post/'+id+'/recommend',
        type: type,
        success: function(result) {
          console.log('recommend post ' + id);
          this.setState({isRecommended: !status});
        }.bind(this)
      });
    }
  },
  toggleInvisiblePost: function(id, status) {
    if (confirm('Make this post invisible to users?')) {
      var type = 'POST';
      if (status === true) {
        type = 'DELETE';
      }
      $.ajax({
        url: '/api/post/'+id+'/invisible',
        type: type,
        success: function(result) {
          console.log('invisible post ' + id);
          this.setState({isInvisible: !status});
        }.bind(this)
      });
    }
  },
  deleteMark: function(id) {
    if (confirm('Delete this mark?')) {
      $.ajax({
        url: '/api/mark/'+id,
        type: 'DELETE',
        success: function(result) {
          console.log('delete mark ' + id);
          React.findDOMNode(this.refs['mark_'+id]).remove();
        }.bind(this)
      });
    }
  },
  nothing: function() {
    console.log('nothing happened');
  },
  render: function() {
    var marks = this.props.marks.map(function (mark) {
      return (
        <li ref={'mark_'+mark.markId} onClick={ this.deleteMark.bind(this, mark.markId) } >{mark.tag}</li>
      );
    }.bind(this));
    var recommendClass = 'post-caption-btn btn btn-default btn-sm';
    if (this.state.isRecommended === true) {
      recommendClass = 'post-caption-btn btn btn-warning btn-sm';
    }
    var invisibleClass = 'post-caption-btn btn btn-default btn-sm';
    if (this.state.isInvisible === true) {
      invisibleClass = 'post-caption-btn btn btn-warning btn-sm';
    }
    return (
      <div ref={'post_'+this.props.postId} className="col-sm-4 col-md-3">
        <div className="panel panel-default">
          <div className="panel-heading post-heading">
            <a href={ '/user/' + this.props.userId } className="media">
              <span className="pull-left media-object thumb thumb-sm">
                <img img-src={'http://cdn.likeorz.com'} src={ this.state.avatar } alt="" className="img-circle" />
              </span>
              <div className="media-body">
                <span className="block">{ this.state.nickname }</span>
                <small className="text-muted">{ this.state.likes }</small>
              </div>
            </a>
          </div>
          <div className="post-image">
            <img src={ this.props.postUrl } />
              <div className="thumb-pane">
                <ul className="marks">{ marks }</ul>
              </div>
              <div className="post-caption">
                <p className="pull-right">
                  <span onClick={ this.nothing } className="post-caption-btn btn btn-default btn-sm" role="button"><span className="glyphicon glyphicon-comment"></span></span>
                  <span onClick={ this.nothing } className="post-caption-btn btn btn-default btn-sm" role="button"><span className="glyphicon glyphicon-tags"></span></span>
                  <span onClick={ this.toggleInvisiblePost.bind(this, this.props.postId, this.state.isInvisible) } className={invisibleClass} role="button"><span className="glyphicon glyphicon glyphicon-eye-close"></span></span>
                  <span onClick={ this.toggleRecommentPost.bind(this, this.props.postId, this.state.isRecommended) } className={recommendClass} role="button"><span className="glyphicon glyphicon-bullhorn"></span></span>
                  <span onClick={ this.deletePost.bind(this, this.props.postId) } className="post-caption-btn btn btn-danger btn-sm" role="button"><span className="glyphicon glyphicon-trash"></span></span>
                </p>
                <div className="clearfix"></div>
            </div>
        </div>
      </div>
    </div>
    );
  }
});

module.exports = PostPanel;
