var $ = require('jquery');
var React = require('react');
var Router = require('react-router');
var ReactRouterBootstrap = require('react-router-bootstrap');
var RouteHandler = Router.RouteHandler;
var NavItemLink = ReactRouterBootstrap.NavItemLink;

var Layout = React.createClass({
  getInitialState: function() {
    return { email: 'Liker', fakeusers: [], fakeuser: null };
  },
  componentDidMount:function() {
    $.get('/admin/email', function(data){
      if (this.isMounted()) {
        var username = data.split('@')[0];
        this.setState({ email: data , username: username});
      }
    }.bind(this));
    $.get('/api/admin/fakeusers', function(data){
      if (this.isMounted()) {
        this.setState({ fakeusers: data, fakeuser: data[0]});
      }
    }.bind(this));
  },
  selectFakeUser: function(fake) {
    this.setState({fakeuser: fake});
  },
  render: function() {
    var selectFakeUserDiv = (<a className="dropdown-toggle" data-toggle="dropdown">Select a fake user</a>);
    if (this.state.fakeuser !== null) {
      selectFakeUserDiv = (
        <a className="dropdown-toggle" data-toggle="dropdown">
          <img src={this.state.fakeuser.avatar} className="user-image" alt="User Image"/>
          <span className="hidden-xs">{this.state.fakeuser.nickname}</span>
        </a>
      );
    }
    var fakeuserInfoDiv = (<ul className="control-sidebar-menu"><li></li></ul>);
    if (this.state.fakeuser !== null) {
      fakeuserInfoDiv = (
        <ul className="control-sidebar-heading">
          <div className="box box-primary">
            <div className="box-body box-profile">
              <img className="profile-user-img img-responsive img-circle" src={this.state.fakeuser.avatar} alt="User profile picture" />
              <h3 className="profile-username text-center">{this.state.fakeuser.nickname}</h3>
              <p className="text-muted text-center">{this.state.fakeuser.mobile}</p>
              <a href="#" className="btn btn-primary btn-block"><b>Profile</b></a>
            </div>
          </div>
        </ul>
      );
    }
    return (
      <div className="wrapper">
        <div className="main-header">
          <a href="/" className="logo">
            <span className="logo-mini"><b>L</b>K</span>
            <span className="logo-lg"><b>Like</b> - dashboard</span>
          </a>
          <nav className="navbar navbar-static-top" role="navigation">
            <a className="sidebar-toggle" data-toggle="offcanvas" role="button">
              <span className="sr-only">Toggle navigation</span>
            </a>
            <div className="navbar-custom-menu">
              <ul className="nav navbar-nav">
                <li className="dropdown user-menu messages-menu">
                  {selectFakeUserDiv}
                  <ul className="dropdown-menu">
                    <li className="header">Select a fake user</li>
                    <li>
                      <ul className="menu">
                        {this.state.fakeusers.map(function (fake) {
                          return (
                            <li key={'fk_'+fake.user_id} onClick={this.selectFakeUser.bind(this, fake)}>
                              <a href="#">
                                <div className="pull-left">
                                  <img src={fake.avatar} className="img-circle" alt="User Image" />
                                </div>
                                <h4>
                                  {fake.nickname}
                                  <small><i className="fa fa-info"></i> {fake.user_id}</small>
                                </h4>
                                <p>{fake.mobile}</p>
                              </a>
                            </li>
                          );
                        }, this)}
                      </ul>
                    </li>
                  </ul>
                </li>
                <li>
                  <a href="#" data-toggle="control-sidebar"><i className="fa fa-gears"></i></a>
                </li>
              </ul>
            </div>
          </nav>
        </div>

        <div className="main-sidebar">
          <section className="sidebar">
            <div className="user-panel">
              <div className="pull-left image">
                <img src="http://cdn.likeorz.com/default_avatar.jpg?imageView2/5/w/80" className="img-circle" alt="User Image" />
              </div>
              <div className="pull-left info">
                <p>{this.state.username}</p>
                <a href="#"><i className="fa fa-circle text-success"></i> Online</a>
              </div>
            </div>
            <ul className="sidebar-menu">
              <li className="header">Menu</li>
              <li><NavItemLink to="home"><i className="fa fa-desktop"></i>Home</NavItemLink></li>
              <li><NavItemLink to="post"><i className="fa fa-camera"></i>Posts</NavItemLink></li>
              <li><NavItemLink to="user"><i className="fa fa-users"></i>Users</NavItemLink></li>
              <li><NavItemLink to="feedback"><i className="fa fa-coffee"></i>Feedback</NavItemLink></li>
              <li><NavItemLink to="report"><i className="fa fa-thumbs-o-down"></i>Report</NavItemLink></li>
              <li><a href="http://monitor.likeorz.com" target="_blank"><i className="fa fa-heartbeat"></i>Monitor</a></li>
              <li><NavItemLink to="taggroup"><i className="fa fa-tags"></i>Tags</NavItemLink></li>
              <li><NavItemLink to="brand"><i className="fa fa-registered"></i>Partners</NavItemLink></li>
              <li className="treeview">
                <a href="#"><i className="fa fa-ellipsis-h"></i> <span>Other</span> <i className="fa fa-angle-left pull-right"></i></a>
                <ul className="treeview-menu">
                  <li><NavItemLink to="activeusers">Active Users</NavItemLink></li>
                  <li><NavItemLink to="judgeposts">Judge Posts</NavItemLink></li>
                </ul>
              </li>
            </ul>
          </section>
        </div>

        <div className="content-wrapper">
          <RouteHandler/>
        </div>

        <footer className="main-footer">
          <div className="pull-right hidden-xs">
            version 1.1.0
          </div>
          Copyright &copy; 2015 Like Co. Ltd All rights reserved.
        </footer>

        <aside className="control-sidebar control-sidebar-dark">
          <ul className="nav nav-tabs nav-justified control-sidebar-tabs">
            <li className="active"><a href="#control-sidebar-home-tab" data-toggle="tab"><i className="fa fa-user"></i></a></li>
            <li><a href="#control-sidebar-settings-tab" data-toggle="tab"><i className="fa fa-gears"></i></a></li>
          </ul>
          <div className="tab-content">
            <div className="tab-pane active" id="control-sidebar-home-tab">
              <h3 className="control-sidebar-heading">Fake user info</h3>
              {fakeuserInfoDiv}
            </div>
            <div className="tab-pane" id="control-sidebar-stats-tab">Stats Tab Content</div>
            <div className="tab-pane" id="control-sidebar-settings-tab">
              <form method="post">
                <h3 className="control-sidebar-heading">General Settings</h3>
                <div className="form-group">
                  <label className="control-sidebar-subheading">
                    Report panel usage
                  </label>
                  <p>Some information about this general settings option</p>
                </div>
              </form>
            </div>
          </div>
        </aside>
        <div className="control-sidebar-bg"></div>
    </div>
    );
  }
});

module.exports = Layout;
