var React = require('react');
var OdometerComponent = require('odometer');

var Odometer = React.createClass({
  componentDidMount: function(){
    this.odometer = new OdometerComponent({
      el: this.getDOMNode(),
      value: this.props.value,
      format: '(,ddd)',
      theme: 'default'
    });
  },
  componentDidUpdate: function() {
    this.odometer.update(this.props.value);
  },
  render: function() {
    return React.DOM.div();
  }
});

module.exports = Odometer;
