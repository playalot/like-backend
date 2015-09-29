var React = require('react');
var Router = require('react-router');
var Route = Router.Route;
var DefaultRoute = Router.DefaultRoute;
var Redirect = Router.Redirect;

var Layout = require('./components/layout');
var Home = require('./components/home');
var PostList = require('./components/postlist');
var UserList = require('./components/userlist');
var ActiveUsers = require('./components/activeusers');
var UserDetail = require('./components/userdetail');
var TagGroup = require('./components/taggroup');
var JudgePosts = require('./components/judgeposts');
var Feedback = require('./components/feedback');
var BrandList = require('./components/brandlist');
var BrandForm = require('./components/brandform');
var Test = require('./components/test');

var routes = (
	<Route name="layout" path="/" handler={Layout}>
		<Route name="home" handler={Home} />
		<Route name="postlist" handler={PostList} />
		<Route name="userlist" handler={UserList} />
		<Route name="userdetail" path="/user/:userId" handler={UserDetail} />
		<Route name="activeusers" path="/activeusers" handler={ActiveUsers} />
		<Route name="taggroup" path="/tags" handler={TagGroup} />
		<Route name="feedback" path="/feedback" handler={Feedback} />
		<Route name="judgeposts" path="/judge" handler={JudgePosts} />
		<Route name="brandlist" path="/brands" handler={BrandList} />
		<Route name="brandform" path="/brandform" handler={BrandForm} />
		<Redirect from="/" to="home" />
		<DefaultRoute handler={Home} />
	</Route>
);

exports.start = function() {
  Router.run(routes, function (Handler) {
		/*jslint browser:true */
		React.render(<Handler/>, document.getElementById('app'));
	});
};
