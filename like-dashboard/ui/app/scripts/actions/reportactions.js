var Reflux = require('reflux');

var ReportActions = Reflux.createActions([
    'fetchReportList',
    'deleteReport',
    'hidePost',
    'deletePost'
]);

module.exports = ReportActions;
