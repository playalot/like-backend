var React = require('react/addons');
var $ = require('jquery');
var Dropzone = require('react-dropzone');

var BrandForm = React.createClass({
  mixins: [React.addons.LinkedStateMixin],
  contextTypes: {
    router: React.PropTypes.func
  },
  getInitialState: function() {
    return {
      id: this.context.router.getCurrentQuery().id,
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
        }.bind(this)
      });
    }
  },
  onDropFile: function(files) {
    // Create a formdata object and add the files
    var data = new FormData();
    var file = files[0];
    // $.each(files, function(key, value) {
    data.append(file.name, file);
    console.log('Received files: ', file.name);
    // });
    this.setState({data: data, file: file.name});
  },
  submitForm: function() {
    console.log('submit update');
    if (this.state.name === '') {
      alert('Name cannot be empty');
      return false;
    }
    var json = {
      id: this.state.id,
      name: this.state.name,
      avatar: this.state.avatar,
      description: this.state.description
    };
    console.log(json);
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
    return false;
  },
  render: function() {
    var text = 'Drop brand image here, or click to select file to upload.';
    if (this.state.file) {
      text = this.state.file;
    }
    return (
      <div>
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
