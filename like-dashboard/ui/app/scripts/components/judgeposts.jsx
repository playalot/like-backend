var React = require('react/addons');
var Reflux = require('reflux');
var Row = require('react-bootstrap').Row;
var Col = require('react-bootstrap').Col;
var ButtonToolbar = require('react-bootstrap').ButtonToolbar;
var JudgePostStore = require('../stores/judgepoststore');
var JudgePostActions = require('../actions/judgepostactions');

var JudgePostList = React.createClass({
  mixins: [Reflux.connect(JudgePostStore, 'postlist')],
  judge: function(id, judge) {
    JudgePostActions.judge(id, judge);
  },
  fetchMorePosts: function() {
    JudgePostActions.fetchPostList();
  },
  render: function() {
    if (this.state.postlist) {
      return (
        <div className="content">
          <Row>
            {this.state.postlist.map(function (post) {
              return (
                <Col xs={12} sm={3} lg={3}>
                  <img src={post.image} alt="..." class="img-rounded" style={{width:'100%'}}/>
                  <ButtonToolbar>
                    <button className="btn btn-success" onClick={this.judge.bind(this, post.id, 1)}>Good</button><button className="btn btn-danger" onClick={this.judge.bind(this, post.id, 0)}>Bad</button>
                  </ButtonToolbar>
                  <br/>
                </Col>
              );
            }, this)}
          </Row>
          <Row>
            <div className="load-more-btn" onClick={this.fetchMorePosts}>Load More</div>
          </Row>
        </div>
      );
    } else {
      return (<Row></Row>);
    }
  }
});

module.exports = JudgePostList;
