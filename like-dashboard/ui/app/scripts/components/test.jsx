var Reflux = require('reflux');
var React = require('react');

var Test = React.createClass({
  render: function() {
    console.log('run test');
    return (
      <div>
        nothing
      </div>
    );
  }
});

module.exports = Test;
