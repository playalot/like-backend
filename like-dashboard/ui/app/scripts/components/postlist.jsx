var React = require('react');
var Reflux = require('reflux');
var Row = require('react-bootstrap').Row;
var PostStore = require('../stores/poststore');
var PostActions = require('../actions/postactions');
var PostPanel = require('./postpanel');

var PostList = React.createClass({
  mixins: [Reflux.connect(PostStore, 'postlist')],
  fetchMorePosts: function() {
    PostActions.fetchPostList();
  },
  render: function() {
    if (this.state.postlist) {
      return (
        <div>
          <Row>
            {this.state.postlist.map(function (post) {
              return (
                <PostPanel key={'p_'+post.id} post={post} />
              );
            })}
          </Row>
          <Row>
            <div className="load-more-post-btn" onClick={this.fetchMorePosts}>Load More</div>
          </Row>
        </div>
      );
    } else {
      return (<Row></Row>);
    }
  }
});

module.exports = PostList;
