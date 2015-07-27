var React = require('react');
var Router = require('react-router');
var Route = Router.Route;
var DefaultRoute = Router.DefaultRoute;
var Redirect = Router.Redirect;

var Layout = require('./components/layout');
var Home = require('./components/home');
var Posts = require('./components/posts');
var Users = require('./components/users');

var routes = (
	<Route name="layout" path="/" handler={Layout}>
		<Route name="posts" path="/posts" handler={Posts} />
		<Route name="users" path="/users" handler={Users} />
		<Route name="home" path="/home" handler={Home} />
		<Redirect from="/" to="home" />
		<DefaultRoute handler={Home} />
	</Route>
);

exports.start = function() {

  Router.run(routes, function (Handler) {
		/*jslint browser:true */
		React.render(<Handler />, document.getElementById('app'));
	});
};
