var React = require('react/addons');
var Reflux = require('reflux');
var Row = require('react-bootstrap').Row;
var Modal = require('react-bootstrap').Modal;
var Input = require('react-bootstrap').Input;
var PostStore = require('../stores/poststore');
var PostActions = require('../actions/postactions');
var PostPanel = require('./postpanel');

var PostList = React.createClass({
  mixins: [Reflux.connect(PostStore, 'postlist'), React.addons.LinkedStateMixin],
  getInitialState: function() {
    return { filter: '', showModal: false, showImage: '' };
  },
  close: function() {
    this.setState({ showModal: false });
  },
  open: function(img) {
    console.log('show image: ' + img);
    var url = img.split('?')[0];
    this.setState({ showModal: true, showImage: url });
  },
  fetchMorePosts: function() {
    PostActions.fetchPostList();
  },
  clickFilter: function(e) {
    console.log('click filter: ' + this.state.filter);
    PostActions.updateParams(this.state.filter.trim());
    PostActions.fetchPostList();
    e.preventDefault();
  },
  render: function() {
    if (this.state.postlist) {
      return (
        <div className="content">
          <div className="page-header">
            <form className="form-input-filter" onSubmit={this.clickFilter}>
              <div className="input-group">
                <Input type='text' placeholder='Search by Tag' valueLink={this.linkState('filter')} />
                <span className="input-group-btn">
                  <button type="button" className="btn btn-info" onClick={this.clickFilter}>Filter</button>
                </span>
              </div>
            </form>
          </div>
          <Row>
            {this.state.postlist.map(function (post) {
              var boundClick = this.open.bind(this, post.content);
              return (
                <PostPanel key={'p_'+post.id} post={post} open={boundClick}/>
              );
            }, this)}
          </Row>
          <Row>
            <div className="load-more-btn" onClick={this.fetchMorePosts}>Load More</div>
          </Row>
          <div>
            <Modal show={this.state.showModal} onHide={this.close}>
              <Modal.Body>
                <img className="image-modal" src={this.state.showImage}/>
              </Modal.Body>
            </Modal>
          </div>
        </div>
      );
    } else {
      return (<Row></Row>);
    }
  }
});

module.exports = PostList;
