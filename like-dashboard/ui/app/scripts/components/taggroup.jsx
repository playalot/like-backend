var React = require('react/addons');
var Reflux = require('reflux');
var Row = require('react-bootstrap').Row;
var Col = require('react-bootstrap').Col;
var Panel = require('react-bootstrap').Panel;
var Input = require('react-bootstrap').Input;
var Modal = require('react-bootstrap').Modal;
var Button = require('react-bootstrap').Button;
var Badge = require('react-bootstrap').Badge;
var TagStore = require('../stores/tagstore');
var TagActions = require('../actions/tagactions');

var TagGroup = React.createClass({
  mixins: [Reflux.connect(TagStore, 'data'), React.addons.LinkedStateMixin],
  getInitialState: function() {
    return {
      tagId: -1,
      inputGroupName: '',
      showModal: false
    };
  },
  fetchMoreTags: function() {
    TagActions.fetchTags();
  },
  setTagGroup: function(tid, gid) {
    TagActions.setTagGroup(tid, gid);
    this.setState({ showModal: false });
  },
  addTagGroup: function(name) {
    TagActions.addTagGroup(name);
  },
  unSetTagGroup: function(tid, gid) {
    TagActions.unSetTagGroup(tid, gid);
  },
  tagSelected: function(id, name) {
    this.setState({tagId: id, tagName: name, showModal: true});
  },
  close: function() {
    this.setState({ showModal: false });
  },
  render: function() {
    if (this.state.data) {
      return (
        <div className="content">
          <Row>
            {this.state.data.groups.map(function(group) {
              return (
                <Col className="col" xs={12} sm={4} lg={4}>
                  <Panel header={group.groupName} bsStyle='success' className='tag-panel'>
                    {group.tags.map(function(tag) {
                      return (<Button onClick={this.unSetTagGroup.bind(this, tag.id, group.groupId)} className='tag-btn' bsSize='small' bsStyle='primary'>{tag.tag}<Badge>{tag.usage}</Badge></Button>);
                    }, this)}
                  </Panel>
                </Col>
              );
            }, this)}
          </Row>
          <Row>
            {this.state.data.tags.map(function(tag) {
              return (
                <Button key={'t_'+tag.id} onClick={this.tagSelected.bind(this, tag.id, tag.tag)} className='tag-btn' bsStyle='primary'>{tag.tag}<Badge>{tag.usage}</Badge></Button>
              );
            }, this)}
          </Row>
          <Row>
            <div className="load-more-btn" onClick={this.fetchMoreTags}>Load More</div>
          </Row>
          <div>
            <Modal className='modal-container' animation={false} show={this.state.showModal} onHide={this.close}>
              <Modal.Body>
                <form className="form-inline">
                  <Input type='text' placeholder='tag group name' valueLink={this.linkState('inputGroupName')} />
                  <Button  bsSize='small' bsStyle='primary' className="btn-raised" onClick={this.addTagGroup.bind(this, this.state.inputGroupName)}>Add</Button>
                  <hr/>
                </form>
                {this.state.data.groups.map(function(group) {
                    return (<Button className='tag-btn' onClick={this.setTagGroup.bind(this, this.state.tagId, group.groupId)} bsStyle='success'>{group.groupName}</Button>);
                }, this)}
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

module.exports = TagGroup;
