var React = require('react');
var Router = require('react-router');

var Link = Router.Link;

var PostPanel = React.createClass({
  getInitialState: function() {
    return {
        nickname: '',
        likes: 0,
        avatar: 'http://cdn.likeorz.com/default_avatar.jpg?imageView2/1/w/40/h/40/q/100'
      };
  },
  componentDidMount: function() {
    $.get('/api/user/'+this.props.userId+'/info', function(data) {
      if (this.isMounted()) {
        this.setState({
          nickname: data.nickname,
          likes: data.likes,
          avatar: data.avatar
        });
      }
    }.bind(this));
  },
  render: function() {
    var marks = this.props.marks.map(function (mark) {
      return (
        <li>{mark.tag}</li>
      );
    });
    return (
      <div className="col-sm-4 col-md-3">
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
            <img src={ this.props.postUrl } data-holder-rendered="true" />
              <div className="thumb-pane">
                <ul className="marks">{ marks }</ul>
              </div>
              <div className="post-caption">
                <p className="pull-right">
                    <a href="/post/recommend?id=20312" title="Recommend" data-confirm="Are you sure you want to recommend this post?" data-method="post" data-pjax="w0" className="btn btn-default btn-sm recommend" role="button"><span className="glyphicon glyphicon-bullhorn"></span></a>
                    <a href="/post/delete?id=20312" title="Delete" data-confirm="Delete this post?" data-method="post" data-pjax="w0" className="btn btn-danger btn-sm" role="button"><span className="glyphicon glyphicon-trash"></span></a>
                </p>
                <p className="pull-left">
                    <a href="/comment/post?id=20312" title="Comment" className="btn btn-default btn-sm" role="button"><span className="glyphicon glyphicon-comment"></span></a>
                    <a href="/mark/post?id=20312" title="Mark" className="btn btn-default btn-sm" role="button"><span className="glyphicon glyphicon-tags"></span></a>
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
