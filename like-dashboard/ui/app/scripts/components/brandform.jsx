var React = require('react/addons');
var Reflux = require('reflux');
var Router = require('react-router');
var $ = require('jquery');
var Dropzone = require('react-dropzone');
var BrandStore = require('../stores/brandstore');
var BrandActions = require('../actions/brandactions');

var BrandForm = React.createClass({
  mixins: [Router.Navigation, Reflux.connect(BrandStore, 'brandlist'), React.addons.LinkedStateMixin],
  contextTypes: {
    router: React.PropTypes.func
  },
  getInitialState: function() {
    return {
      id: this.context.router.getCurrentParams().brandId,
      name: '',
      avatar: '',
      description : '',
      data: null,
      image: null
    };
  },
  componentDidMount: function() {
    if (this.state.id) {
      $.ajax({
        url: '/api/brand/'+this.state.id,  //Server script to process data
        type: 'GET',
        success: function (data) {
          console.log(data);
          this.setState({
            name: data.name,
            avatar: data.avatar,
            description: data.description,
            image: data.image
          });
        }.bind(this)
      });
    }
  },
  uploadFile: function(id) {
    console.log(this.state.data);
    if (this.state.data) {
      $.ajax({
        url: '/api/brand/'+id+'/upload',  //Server script to process data
        type: 'POST',
        data: this.state.data,
        cache: false,
        contentType: false,
        processData: false,
        success: function (res) {
          console.log(res);
          this.setState({avatar: res});
          location.reload();
        }.bind(this)
      });
    }
  },
  onDropFile: function(files) {
    // Create a formdata object and add the files
    var formData = new FormData();
    var file = files[0];
    formData.append("file", file);
    this.setState({data: formData, file: file.name});
  },
  submitForm: function() {
    console.log('submit update');
    if (this.state.name === '') {
      alert('Name cannot be empty');
      return false;
    }
    var json = {
      name: this.state.name,
      avatar: this.state.avatar,
      description: this.state.description
    };
    if (this.state.id) {
      $.ajax({
        type: 'POST',
        url: '/api/brand/' + this.state.id,
        data: JSON.stringify(json),
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        success: function(brand) {
          console.log(brand);
          this.uploadFile(brand.id);
          BrandActions.reloadBrandList();
        }.bind(this)
      });
    } else {
      $.ajax({
        type: 'POST',
        url: '/api/brand',
        data: JSON.stringify(json),
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        success: function(brand) {
          console.log(brand);
          this.setState({id:brand.id});
          this.uploadFile(brand.id);
        }.bind(this)
      });
    }
    return false;
  },
  render: function() {
    var text = 'Drop brand image here, or click to select file to upload.';
    if (this.state.file) {
      text = this.state.file;
    }
    return (
      <div className="content">
        <h1>Brand Form</h1>
        <form id="brandForm">
          <input type="hidden" name="id" valueLink={this.linkState('id')} />
          <div className="form-group">
            <label>Brand Name</label>
            <input type="text" className="form-control" name="name" valueLink={this.linkState('name')} />
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea className="form-control" name="description" valueLink={this.linkState('description')} />
          </div>
          <div className="form-group">
            <label>Qiniu Name: <i>{this.state.avatar}</i></label>
          </div>
          <Dropzone onDrop={this.onDropFile} width={350} height={50}>
            <div>{text}</div>
          </Dropzone>
          <img className="img-thumbnail" src={this.state.image}/>
          <br/><br/>
          <button className="btn btn-default" onClick={this.submitForm}>Submit</button>
        </form>
      </div>
    );
  }
});

module.exports = BrandForm;
