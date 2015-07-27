var $ = require('jquery');
var React = require('react');
var Router = require('react-router');
var ReactRouterBootstrap = require('react-router-bootstrap');

var RouteHandler = Router.RouteHandler;
var NavItemLink = ReactRouterBootstrap.NavItemLink;
var Layout = React.createClass({
  getInitialState: function() {
    return { email:'Liker' };
  },
  componentDidMount:function() {
    $.get('/admin/email', function(data){
      if (this.isMounted()) {
        this.setState({email: data});
      }
    }.bind(this));
  },
  render: function() {

    return (
      <div className="wrap">
        <nav id="w0" className="navbar-inverse navbar-fixed-top navbar" role="navigation">
          <div className="container">
            <div className="navbar-header">
              <button type="button" className="navbar-toggle collapsed" data-toggle="collapse" data-target="#w0-collapse" aria-expanded="false">
                <span className="sr-only">Toggle navigation</span>
                <span className="icon-bar"></span>
                <span className="icon-bar"></span>
                <span className="icon-bar"></span>
              </button>
              <a className="navbar-brand" href="/">Like - Dashboard</a>
              <div id="w0-collapse" className="collapse navbar-collapse">
                <ul id="w1" className="navbar-nav navbar-right nav">
                  <li><NavItemLink to="home" >Home</NavItemLink></li>
                  <li><NavItemLink to="users" >Users</NavItemLink></li>
                  <li><NavItemLink to="posts" >Posts</NavItemLink></li>
                  <li className="dropdown">
                    <a className="dropdown-toggle" href="#" data-toggle="dropdown" aria-expanded="false" role="button"> {this.state.email} <span className="caret"></span></a>
                    <ul id="w2" className="dropdown-menu"><li><a href="/signOut" data-method="post" tabindex="-1">Logout</a></li></ul>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </nav>
        <div className="container">
          <RouteHandler/>
        </div>
      </div>
    );
  }
});

module.exports = Layout;
