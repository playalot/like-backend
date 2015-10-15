var React = require('react/addons');
var Reflux = require('reflux');
var Row = require('react-bootstrap').Row;
var Input = require('react-bootstrap').Input;
var BanUserStore = require('../stores/banuserstore');
var BanUserActions = require('../actions/banuseractions');
var Moment = require('moment');

var BanUserList = React.createClass({
  mixins: [Reflux.connect(BanUserStore, 'banusers'), React.addons.LinkedStateMixin],
  getInitialState: function() {
    return { banid: '' };
  },
  banUser: function() {
    BanUserActions.banUser(this.state.banid);
  },
  removeBanUser: function(id) {
    BanUserActions.removeBanUser(id);
  },
  render: function() {
    if (this.state.banusers) {
      return (
        <div className="content">
          <Row>
            <h5 className="col-sm-6">Total {this.state.banusers.length} users are banned</h5>
            <form className="form-input-filter col-sm-6" onSubmit={this.clickFilter}>
              <div className="input-group">
                <Input type='text' placeholder='Input user id to be banned' valueLink={this.linkState('banid')}  />
                <span className="input-group-btn">
                  <button type="button" className="btn btn-danger" onClick={this.banUser}>Ban</button>
                </span>
              </div>
            </form>
          </Row>
          <div className="table-responsive">
            <table className="table table-striped">
              <thead><tr><th>#ID</th><th>Avatar</th><th>Nickname</th><th>Likes</th><th>Action</th></tr></thead>
              <tbody>
                {this.state.banusers.map(function (user) {
                  return (
                    <tr key={'u_'+user.id}><td>{user.id}</td><td><a href={'/#/user/'+user.id}><img src={user.avatar} className="img-circle"/></a></td><td>{user.nickname}</td><td>{user.likes}</td><td><span className="btn btn-danger" onClick={this.removeBanUser.bind(this, user.id)} >Remove</span></td></tr>
                  );
                }, this)}
                <tr></tr>
              </tbody>
            </table>
          </div>
        </div>
      );
    } else {
      return (<Row></Row>);
    }
  }
});

module.exports = BanUserList;
