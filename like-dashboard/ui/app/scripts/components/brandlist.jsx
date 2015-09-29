var React = require('react');
var Reflux = require('reflux');
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
    if (this.state.brandlist) {
      return (
        <div className="content">
          <p>
            <a href="/brandform"><Button bsStyle='success'>Create new brand</Button></a>
          </p>
            {this.state.brandlist.map(function (brand) {
                var promoteBtn = <a className="btn btn-sm" onClick={this.togglePromoteBrand.bind(this, brand.id)}>Promote</a>;
                if (brand.isPromoted) {
                  promoteBtn = <a className="btn btn-sm btn-yellow" onClick={this.togglePromoteBrand.bind(this, brand.id)}>Promoted</a>;
                }
                return (
                  <Row key={'b_'+brand.id}>
                    <Col xs={2} sm={2} lg={2}><img className="thumbnail" src={brand.avatar} /></Col>
                    <Col className="col" xs={2} sm={2} lg={2}><span>{brand.name}</span></Col>
                    <Col className="col" xs={4} sm={4} lg={4}><span>{brand.description}</span></Col>
                    <col xs={4} sm={4} lg={4}>
                      <ButtonToolbar>
                        <a href={'/brandform?id='+brand.id} className="btn btn-cyan btn-sm">Edit</a>
                        {promoteBtn}
                        <a className="btn btn-red btn-sm" onClick={this.deleteBrand.bind(this, brand.id)}>Delete</a>
                      </ButtonToolbar>
                    </col>
                  </Row>
                );
            }.bind(this))}
            <Row>
              <div className="load-more-btn" onClick={this.fetchMoreBrands}>Load More</div>
            </Row>
        </div>
      );
    } else {
      return (<div></div>);
    }
  }
});

module.exports = Brands;
