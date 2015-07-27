var $ = require('jquery');
var React = require('react');
var Router = require('react-router');

var PostPanel = require('../widgets/postPanel.jsx');

var Link = Router.Link;

var Posts = React.createClass({
  getInitialState: function() {
    return {
      data: {
        page: { page: 0, total: 0, prev: null, next: 1},
        posts: []
      }
    };
  },
  componentDidMount:function() {
    $.get('/api/posts/'+ this.state.data.page.page, function(data){
      if (this.isMounted()) {
        this.setState({ data: data });
      }
    }.bind(this));
  },
  render: function() {
    var postDivs = this.state.data.posts.map(function (post) {
      return (
        <PostPanel marks={post.marks} userId={post.user_id} postId={post.post_id} postUrl={post.post_url} key={'p_'+post.post_id}/>
      )
    });
    return (
      <div>
        <h1>Posts </h1>
        <hr/>
        {postDivs}
      </div>
    );
  }
});

module.exports = Posts;
