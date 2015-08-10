var Reflux = require('reflux');

var BrandActions = Reflux.createActions([
    'fetchBrandList',
    'deleteBrand',
    'togglePromoteBrand'
]);

module.exports = BrandActions;
