var $ = require('jquery');
var React = require('react');
var PostPanel = require('../widgets/postPanel');
var Paginator = require('../widgets/pagination');

var Posts = React.createClass({
  getInitialState: function() {
    return {
      page: { page: 0, total: 1000, prev: null, next: 1, pageSize: 30 },
      posts: []
    };
  },
  componentDidMount:function() {
    this.onChangePage(0);
  },
  onChangePage: function(v) {
    $.get('/api/posts/'+ v, function(data){
      if (this.isMounted()) {
        this.setState({ page: data.page, posts: data.posts });
      }
    }.bind(this));
  },
  render: function() {
    var postDivs = this.state.posts.map(function(post) {
      return (
        <PostPanel marks={post.marks} userId={post.userId} postId={post.postId} postUrl={post.postUrl} key={'p_'+post.postId} />
      );
    });
    var end = 1;
    if (this.state.page.next !== null) {
      end = this.state.page.currentPage * this.state.page.pageSize + this.state.page.pageSize;
    } else {
      end = this.state.page.total;
    }
    var pageItems = Math.ceil(this.state.page.total * 1.0 / this.state.page.pageSize);
    return (
      <div>
        <h1>Posts</h1>
        <hr/>
        <div>
          <div className="list-view row">
            <div className="post-summary">Showing <b>{this.state.page.currentPage * this.state.page.pageSize + 1}-{ end }</b> of <b>{this.state.page.total}</b> items.</div>
            { postDivs }
          </div>
          <Paginator currentPage={this.state.page.currentPage} max={pageItems} maxVisible={7} onChange={this.onChangePage}/>
        </div>
      </div>
    );
  }
});

module.exports = Posts;
