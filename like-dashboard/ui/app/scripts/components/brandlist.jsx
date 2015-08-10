var React = require('react');
var Reflux = require('reflux');
var Link = require('react-router').Router;
var Row = require('react-bootstrap').Row;
var Col = require('react-bootstrap').Col;
var Button = require('react-bootstrap').Button;
var ButtonToolbar = require('react-bootstrap').ButtonToolbar;
var BrandStore = require('../stores/brandstore');
var BrandActions = require('../actions/brandactions');

var Brands = React.createClass({
  mixins: [Reflux.connect(BrandStore, 'brandlist')],
  fetchMoreBrands: function() {
    BrandActions.fetchBrandList();
  },
  togglePromoteBrand: function(id) {
    BrandActions.togglePromoteBrand(id);
  },
  deleteBrand: function(id) {
    if (confirm('Delete this brand?')) {
      BrandActions.deleteBrand(id);
    }
  },
  render: function() {
    return (
      <div>
        <p>
          <a href="/brandform"><Button bsStyle='success'>Create new brand</Button></a>
        </p>
          {this.state.brandlist.map(function (brand) {
              var promoteBtn = <Button onClick={this.togglePromoteBrand.bind(this, brand.id)}>Promote</Button>;
              if (brand.isPromoted) {
                promoteBtn = <Button bsStyle='success' onClick={this.togglePromoteBrand.bind(this, brand.id)}>Promoted</Button>;
              }
              return (
                <Row key={'b_'+brand.id}>
                  <Col xs={2} sm={2} lg={2}><img className="thumbnail" src={brand.avatar} /></Col>
                  <Col className="col" xs={2} sm={2} lg={2}><span>{brand.name}</span></Col>
                  <Col className="col" xs={4} sm={4} lg={4}><span>{brand.description}</span></Col>
                  <col xs={4} sm={4} lg={4}>
                    <ButtonToolbar>
                      <a href={'/brandform?id='+brand.id}><Button>Edit</Button></a>
                      {promoteBtn}
                      <Button bsStyle='danger' onClick={this.deleteBrand.bind(this, brand.id)}>Delete</Button>
                    </ButtonToolbar>
                  </col>
                </Row>
              );
          }.bind(this))}
          <Row>
            <div className="load-more-post-btn" onClick={this.fetchMoreBrands}>Load More</div>
          </Row>
      </div>
    );
  }
});

module.exports = Brands;
