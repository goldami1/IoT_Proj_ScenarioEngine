import React, { Component } from 'react';
import { connect } from "react-redux";
import { addDevice , fetchVendors, fetchProducts } from "../../../actions/devices_actions";
import { Link } from 'react-router-dom';
import { withRouter } from "react-router-dom";

class DeviceForm extends Component {


	constructor(props){
		super(props);
		this.state = {
			vendor: '',
			product:'',
			serial:'',
			error:'',
			isLoading:false
		}
		this.onChange = this.onChange.bind(this);
		this.handleChange = this.handleChange.bind(this);
		this.onSubmit = this.onSubmit.bind(this);
	}

	onChange( event ){
		this.setState({
			[event.target.name] : event.target.value
		}) 
	}

	handleChange( event ){
		this.setState({
			[event.target.name] : event.target.value
		}),
		this.setState({ isLoading: true });
		this.props.fetchProducts(event.target.value)
			.then((res) => this.setState({ isLoading: false }));
	}

	onSubmit (event ){
		event.preventDefault();
		this.setState({
			error: '',
			isLoading: true 
		});
		this.props.addDevice(this.state)
			.then(
				(res) => {
					this.props.history.goBack();
					this.setState({
						isLoading: false 
					});
				},
				(err) => {
					this.setState({
						error: err.response.data.error,
						isLoading: false
					});
				}
			);

	}

	componentDidMount() {
		this.setState({ isLoading: true });
		this.props.fetchVendors()
			.then((res) => this.setState({ isLoading: false }));

	}


	render() {

 	// 	// TODO :: make reusable funtion
		const vendorOptions = this.props.vendors.map(vendor => {
			return (
				<option value={vendor.id}>
					{vendor.name}
				</option>
				);
			}
		);

		const productOptions = this.props.products.map(product => {
			return (
				<option value={product.id}>
					{product.name}
				</option>
				);
			}
		);

		return (
			<form 
				onSubmit={this.onSubmit} 
				className=" col-md-4 offset-md-4 m-5 card card-body  ">

				<div className="form-group">
					<label >Vendor</label>
					<select 
						className="custom-select w-100"
						onChange={this.handleChange}
						name="vendor"
						value={this.state.vendor}>
						{vendorOptions}
					</select>
				</div>

				<div className="form-group">
					<label >Vendor</label>
					<select 
						onChange={this.onChange}
						className="custom-select w-100" 
						value={this.state.product}
						name="product" 
						>
						{productOptions}
					</select>
				</div>
				<div className="form-group">
					<label >Serial</label>
					<input 
						onChange={this.onChange}
						value={this.state.serial}
						type="text" 
						className="form-control" 
						name="serial"  
						placeholder="serial number"/>
				</div>
				<button 

					disabled={this.state.isLoading}
					type="submit" 
					className="mt-3 btn btn-primary btn-lg btn-block ">
					Add device
				</button>
				<div className=" m-3 text-center" >
				{
					this.state.error &&
             	  	<div className=" m-3  text-danger">
                        {this.state.error}
                    </div>	
				}
				</div>
			</form>
		);
	}
}

function mapStateToProps({devices}) {
	return {
		vendors: devices.vendors,
		products: devices.products,
	}
}


export default connect(mapStateToProps, { addDevice , fetchVendors, fetchProducts })(withRouter(DeviceForm));