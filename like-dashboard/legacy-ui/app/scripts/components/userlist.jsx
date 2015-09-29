var React = require('react/addons');
var Reflux = require('reflux');
var Row = require('react-bootstrap').Row;
var Input = require('react-bootstrap').Input;
var UserStore = require('../stores/userliststore');
var UserActions = require('../actions/useractions');

var UserList = React.createClass({
  mixins: [Reflux.connect(UserStore, 'userlist'), React.addons.LinkedStateMixin],
  getInitialState: function() {
    return { filter: '' };
  },
  fetchMoreUsers: function() {
    UserActions.fetchUserList();
  },
  clickFilter: function(e) {
    console.log('click filter: ' + this.state.filter);
    UserActions.updateParams(this.state.filter.trim());
    UserActions.fetchUserList();
    e.preventDefault();
  },
  render: function() {
    if (this.state.userlist) {
      return (
        <div>
          <div className="page-header">
            <form className="form-inline form-input-filter">
              <Input type='text' placeholder='Search User' valueLink={this.linkState('filter')} />
              <button className="btn btn-primary" onClick={this.clickFilter}>Filter</button>
            </form>
          </div>
          <div className="table-responsive">
            <table className="table table-striped">
              <thead><tr><th>#ID</th><th>Avatar</th><th>Nickname</th><th>Mobile</th><th>Likes</th></tr></thead>
              <tbody>
                {this.state.userlist.map(function (user) {
                  return (
                    <tr key={'u_'+user.id}><td>{user.id}</td><td><a href={'/user/'+user.id}><img src={user.avatar} className="img-circle"/></a></td><td>{user.nickname}</td><td>{user.mobile}</td><td>{user.likes}</td></tr>
                  );
                })}
                <tr></tr>
              </tbody>
            </table>
          </div>
          <Row>
            <div className="load-more-btn" onClick={this.fetchMoreUsers}>Load More</div>
          </Row>
        </div>
      );
    } else {
      return (<Row></Row>);
    }
  }
});

module.exports = UserList;
