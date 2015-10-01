var React = require('react/addons');
var Reflux = require('reflux');
var Row = require('react-bootstrap').Row;
var Modal = require('react-bootstrap').Modal;
var UserDetailStore = require('../stores/userdetailstore');
var UserDetailActions = require('../actions/userdetailactions');
var PostPanel = require('./postpanel');

var UserDetail = React.createClass({
  contextTypes: {
    router: React.PropTypes.func
  },
  mixins: [Reflux.connect(UserDetailStore, 'userDetail'), React.addons.LinkedStateMixin],
  getInitialState: function() {
    return { filter: '', showModal: false, showImage: '' };
  },
  componentWillMount: function() {
    console.log(this.context.router.getCurrentParams().userId);
    UserDetailActions.updateUserId(this.context.router.getCurrentParams().userId);
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
    UserDetailActions.fetchUserPosts();
  },
  render: function() {
    if (this.state.userDetail.userPostlist) {
      return (
        <div className="content">
          <div className="page-header">
            <img src={this.state.userDetail.userInfo.avatar} width="100" height="100" className="img-circle img-corona" alt="user-avatar" />
      			<span className="user-profile">
      				<h2 className="block">{this.state.userDetail.userInfo.nickname} <small>{this.state.userDetail.userInfo.likes} likes</small></h2>
      				<small>{this.state.userDetail.userInfo.count.posts} posts | {this.state.userDetail.userInfo.count.followers} followers | {this.state.userDetail.userInfo.count.following} following</small>
      			</span>
          </div>
          <Row>
            {this.state.userDetail.userPostlist.map(function (post) {
              var boundClick = this.open.bind(this, post.content);
              return (
                <PostPanel key={'p_'+post.id} post={post} open={boundClick} showUser={false}/>
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

module.exports = UserDetail;
