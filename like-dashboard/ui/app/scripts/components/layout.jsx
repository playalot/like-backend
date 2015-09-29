var $ = require('jquery');
var React = require('react');
var Router = require('react-router');
var ReactRouterBootstrap = require('react-router-bootstrap');
var RouteHandler = Router.RouteHandler;
var NavItemLink = ReactRouterBootstrap.NavItemLink;

var Layout = React.createClass({
  getInitialState: function() {
    return { email: 'Liker' };
  },
  componentDidMount:function() {
    $.get('/admin/email', function(data){
      if (this.isMounted()) {
        var username = data.split('@')[0];
        this.setState({ email: data , username: username});
      }
    }.bind(this));
  },
  render: function() {

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
                <li className="dropdown user user-menu">
                  <a href="#" className="dropdown-toggle" data-toggle="dropdown">
                    <img src="http://cdn.likeorz.com/default_avatar.jpg?imageView2/5/w/80" className="user-image" alt="User Image"/>
                    <span className="hidden-xs">{this.state.email}</span>
                  </a>
                  <ul className="dropdown-menu">
                    <li className="user-header">
                      <img src="http://cdn.likeorz.com/default_avatar.jpg?imageView2/5/w/80" className="img-circle" alt="User Image"/>
                      <p>
                        <span>Alexander Pierce - Web Developer</span>
                        <small>Member since Nov. 2012</small>
                      </p>
                    </li>
                    <li className="user-body">
                      <div className="col-xs-4 text-center">
                        <a href="#">Followers</a>
                      </div>
                    </li>
                    <li className="user-footer">
                      <div className="pull-left">
                        <a href="#" className="btn btn-default btn-flat">Profile</a>
                      </div>
                      <div className="pull-right">
                        <a href="#" className="btn btn-default btn-flat">Sign out</a>
                      </div>
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
              <li><NavItemLink to="postlist"><i className="fa fa-camera"></i>Posts</NavItemLink></li>
              <li><NavItemLink to="userlist"><i className="fa fa-users"></i>Users</NavItemLink></li>
              <li><NavItemLink to="feedback"><i className="fa fa-coffee"></i>Feedback</NavItemLink></li>
              <li><a href="http://monitor.likeorz.com" target="_blank"><i className="fa fa-heartbeat"></i>Monitor</a></li>
              <li><NavItemLink to="taggroup"><i className="fa fa-tags"></i>Tags</NavItemLink></li>
              <li><NavItemLink to="brandlist"><i className="fa fa-registered"></i>Partners</NavItemLink></li>
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
            <li className="active"><a href="#control-sidebar-home-tab" data-toggle="tab"><i className="fa fa-home"></i></a></li>
            <li><a href="#control-sidebar-settings-tab" data-toggle="tab"><i className="fa fa-gears"></i></a></li>
          </ul>
          <div className="tab-content">
            <div className="tab-pane active" id="control-sidebar-home-tab">
              <h3 className="control-sidebar-heading">Recent Activity</h3>
              <ul className="control-sidebar-menu">
                <li>
                  <a href="javascript::;">
                    <i className="menu-icon fa fa-birthday-cake bg-red"></i>
                    <div className="menu-info">
                      <h4 className="control-sidebar-subheading">Langdon's Birthday</h4>
                      <p>Will be 23 on April 24th</p>
                    </div>
                  </a>
                </li>
              </ul>

              <h3 className="control-sidebar-heading">Tasks Progress</h3>
              <ul className="control-sidebar-menu">
                <li>
                  <a href="javascript::;">
                    <h4 className="control-sidebar-subheading">
                      Custom Template Design
                      <span className="label label-danger pull-right">70%</span>
                    </h4>
                    <div className="progress progress-xxs">
                      <div className="progress-bar progress-bar-danger" style={{'width': '70%'}}></div>
                    </div>
                  </a>
                </li>
              </ul>

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
