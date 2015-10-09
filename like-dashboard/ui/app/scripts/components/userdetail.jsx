var React = require('react/addons');
var Reflux = require('reflux');
var Row = require('react-bootstrap').Row;
var Col = require('react-bootstrap').Col;
var Input = require('react-bootstrap').Input;
var Modal = require('react-bootstrap').Modal;
var Tab = require('react-bootstrap').Tab;
var Tabs = require('react-bootstrap').Tabs;
var UserDetailStore = require('../stores/userdetailstore');
var UserDetailActions = require('../actions/userdetailactions');
var FakeUserStore = require('../stores/fakeuserstore');
var PostPanel = require('./postpanel');

var UserDetail = React.createClass({
  contextTypes: {
    router: React.PropTypes.func
  },
  mixins: [Reflux.connect(UserDetailStore, 'userDetail'), Reflux.connect(FakeUserStore, 'fake'), React.addons.LinkedStateMixin],
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
    console.log(this.state.userDetail);
    if (this.state.userDetail.user) {
      return (
        <div className="content">
          <div className="page-header">
            <img src={this.state.userDetail.user.avatar} width="100" height="100" className="img-circle img-corona" alt="user-avatar" />
      			<span className="user-profile">
      				<h2 className="block">{this.state.userDetail.user.nickname} <small>{this.state.userDetail.user.likes} likes</small></h2>
      				<small>{this.state.userDetail.user.count.posts} posts | {this.state.userDetail.user.count.followers} followers | {this.state.userDetail.user.count.following} following</small>
      			</span>
          </div>
          <Row>
            <Col className="col" xs={12} sm={12} lg={12}>
              <Tabs defaultActiveKey={1} className="nav-tabs-custom">
                <Tab eventKey={1} title="Posts">
                  <Row>
                    {this.state.userDetail.postlist.map(function (post) {
                      var boundClick = this.open.bind(this, post.content);
                      return (
                        <PostPanel key={'p_'+post.id} post={post} open={boundClick} showUser={false} fakeuserId={this.state.fake.fakeuser.user_id}/>
                      );
                    }, this)}
                  </Row>
                  <Row>
                    <div className="load-more-btn" onClick={this.fetchMorePosts}>Load More</div>
                  </Row>
                </Tab>
                <Tab eventKey={2} title="Settings">
                  <form className="form-horizontal">
                    <div className="form-group">
                      <label for="inputName" className="col-sm-2 control-label">Nickname</label>
                      <div className="col-sm-10">
                        <input type="text" className="form-control" placeholder="Nickname" value={this.state.userDetail.user.nickname}/>
                      </div>
                    </div>
                    <div className="form-group">
                      <label for="inputEmail" className="col-sm-2 control-label">Email</label>
                      <div className="col-sm-10">
                        <input type="email" className="form-control" placeholder="Email" value={this.state.userDetail.user.email}/>
                      </div>
                    </div>
                    <div className="form-group">
                      <label for="inputMobile" className="col-sm-2 control-label">Mobile</label>
                      <div className="col-sm-10">
                        <input type="text" className="form-control" id="inputName" placeholder="Mobile" value={this.state.userDetail.user.mobile} />
                      </div>
                    </div>
                    <div className="form-group">
                      <div className="col-sm-offset-2 col-sm-10">
                        <button type="submit" className="btn btn-danger">Submit</button>
                      </div>
                    </div>
                  </form>
                </Tab>
              </Tabs>
            </Col>
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
