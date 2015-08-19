var React = require('react');
var Router = require('react-router');
var Route = Router.Route;
var DefaultRoute = Router.DefaultRoute;
var Redirect = Router.Redirect;

var Layout = require('./components/layout');
var Home = require('./components/home');
var PostList = require('./components/postlist');
var UserList = require('./components/userlist');
var UserDetail = require('./components/userdetail');
var BrandList = require('./components/brandlist');
var BrandForm = require('./components/brandform');

var routes = (
	<Route name="layout" path="/" handler={Layout}>
		<Route name="postlist" path="/posts" handler={PostList} />
		<Route name="userlist" path="/users" handler={UserList} />
		<Route name="userdetail" path="/user/:userId" handler={UserDetail} />
		<Route name="brandlist" path="/brands" handler={BrandList} />
		<Route name="brandform" path="/brandform" handler={BrandForm} />
		<Route name="home" path="/home" handler={Home} />
		<Redirect from="/" to="home" />
		<DefaultRoute handler={Home} />
	</Route>
);

exports.start = function() {
  Router.run(routes, Router.HistoryLocation, function (Handler) {
		/*jslint browser:true */
		React.render(<Handler />, document.getElementById('app'));
	});
};
