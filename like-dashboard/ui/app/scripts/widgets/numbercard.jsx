var React = require('react');

var NumberCard = React.createClass({
  getInitialState: function() {
    return {data: ''};
  },
  componentDidMount: function() {
    this.loadData();
  },
  loadData: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  render: function() {
    return (
      <div className="info-box">
        <span className={'info-box-icon bg-'+this.props.color}><i className={'fa fa-'+this.props.icon}></i></span>
        <div className="info-box-content">
          <span className="info-box-text">{this.props.title}</span>
          <span className="info-box-number">{this.state.data}</span>
        </div>
      </div>
    );
  }
});

module.exports = NumberCard;
