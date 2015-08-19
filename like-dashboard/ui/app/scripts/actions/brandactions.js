var Reflux = require('reflux');

var BrandActions = Reflux.createActions([
    'fetchBrandList',
    'reloadBrandList',
    'deleteBrand',
    'togglePromoteBrand'
]);

module.exports = BrandActions;
