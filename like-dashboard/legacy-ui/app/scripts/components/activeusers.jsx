var React = require('react/addons');
var Reflux = require('reflux');
var Row = require('react-bootstrap').Row;
var ActiveUsersStore = require('../stores/activeusersstore');
var Moment = require('moment');

var ActiveUserList = React.createClass({
  mixins: [Reflux.connect(ActiveUsersStore, 'activeusers')],
  render: function() {
    if (this.state.activeusers) {
      return (
        <div>
          <h5>Total {this.state.activeusers.total} users are active within the last 24 hours</h5>
          <div className="table-responsive">
            <table className="table table-striped">
              <thead><tr><th>#ID</th><th>Avatar</th><th>Nickname</th><th>Mobile</th><th>Likes</th></tr></thead>
              <tbody>
                {this.state.activeusers.userlist.map(function (user) {
                  return (
                    <tr key={'u_'+user.id}><td>{user.id}</td><td><a href={'/user/'+user.id}><img src={user.avatar} className="img-circle"/></a></td><td>{user.nickname}</td><td>{Moment.unix(user.lastSeen).fromNow()}</td><td>{user.likes}</td></tr>
                  );
                })}
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

module.exports = ActiveUserList;
