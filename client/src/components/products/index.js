import React, { Component } from 'react';
import {List, Tabs, Upload, Icon, message,Button, Modal, Form, Input, Radio ,Row, Col, Tag, Tooltip,Collapse} from 'antd';
import { connect } from "react-redux";
import  ContentWrapper from "../common/ContentWrapper";

const {Panel} = Collapse.Panel;

const FormItem = Form.Item;
const { TextArea } = Input;

function getBase64(img, callback) {
	const reader = new FileReader();
	reader.addEventListener('load', () => callback(reader.result));
	reader.readAsDataURL(img);
}


class ProductsPage extends Component {

	constructor(props){
		super(props);
		this.state = {
			name:'',
			description:'',
			endpoint:'',
			image:'',
			actions:[
				{
					name:'',
					description:'',
					properties: [
						{
							name: '',
							description:'',
							type:'',
							options: []
						}
					]
				}
			],
			events:[
				{
					name:'',
					description:'',
					properties: [
						{
							name: '',
							description:'',
							type:'',
							options: []
						}
					]
				}
			],

			inputValue:'',
			tags: ['Unremovable', 'Tag 2', 'Tag 3'],
			inputVisible: false,
			loading: false,
		};

	}

	handleChange = (info) => {
		if (info.file.status === 'uploading') {
			this.setState({ loading: true });
			return;
		}
		if (info.file.status === 'done') {
			// Get this url from response in real world.
			getBase64(info.file.originFileObj, image => this.setState({
				image,
				loading: false,
			}));
		}
	}

	handleInputConfirm = () => {

		const state = this.state;
		const inputValue = state.inputValue;
		let tags = state.tags;
		if (inputValue && tags.indexOf(inputValue) === -1) {
			tags = [...tags, inputValue];
		}
		console.log(tags);
		this.setState({
			tags,
			inputVisible: false,
			inputValue: '',
		});
	}

	saveInputRef = input => this.input = input
		showInput = () => {
		this.setState({ inputVisible: true }, () => this.input.focus());

	}

	hideInput = () => {
		this.setState({ inputVisible: false,inputValue:'' });
	}

	onChange = (event) =>{
		this.setState({
			[event.target.name] : event.target.value
		}) 
	}

	handleRemoveAE = (aeIndex,ae) => () => {
		this.setState({
			[ae]: this.state[ae].filter((s, saeIndex) => aeIndex !== saeIndex)
		});
	}


	handleRemoveProperty = (aeIndex,aePropId,ae) => (event) => {
		this.setState({
			[ae]: this.state[ae].map((singleAe, index) => {
				var prop;
				if(index === aeIndex){
					prop = singleAe.properties.filter((property,pindex) => pindex !== aePropId)
					return {
						...singleAe, properties:prop
					}	
				}
				return singleAe;
			})

		});
	}
	handleChangeProperty = (aeIndex,aePropId,ae,isOption) => (event) => {
		console.log(aeIndex,aePropId,isOption,ae);
		const value = this.state.inputValue;
		this.setState({
			actions: this.state.actions.map((action, index) => {
				var prop;
				if(index === aeIndex){
					prop = action.properties.map((property,pindex) => {
						if (pindex === aePropId) {

							if (isOption) {
								return {
									...property,
									options:[...property.options, value]							
								};								
							}
							return {
								...property, [event.target.name]: event.target.value
							};
						}
						return property;
					})
					return {
						...action, properties:prop
					}	
				}
				return action;
			})

		});
	}


	handleChangeAE = (aeIndex,ae) => (event) => {
		const newAE = this.state[ae].map((singleAe, saeIndex) => {
		if (aeIndex !== saeIndex) return singleAe;
			return { ...singleAe, [event.target.name]: event.target.value };
		});

		this.setState({ [ae]: newAE });
	}




	handleAddProperty = (aeIndex,ae) => ()=>{
		console.log(this.state.actions[aeIndex]);
		this.setState({
			[ae]: this.state[ae].map((singleAe, index) => {
				if(index === aeIndex){
					return {
						...singleAe,
						properties: [...singleAe.properties,
						{
							name: '',
							description:'',
							type:'',
							options:[]
						}]
					};
				}
				return singleAe;
			})
		});
	}

	handleAddAE = (ae) => () => {
		this.setState({
			[ae]: this.state[ae].concat([
				{
					name:'',
					description:'',
					properties: [
						{
							name: '',
							description:'',
							type:'',
							options:[]
						}
					]
				}
			])
		});
	}


	handleClose = (aeIndex,aePropId,optionId,ae) => (event) => {
		this.setState({
			[ae]: this.state[ae].map((singleAe, index) => {
				var prop,opts;
				if(index === aeIndex){
					prop = singleAe.properties.map((property,pindex) => {
						if (pindex === aePropId) {
							opts = property.options.filter((option,oindex) => oindex !== optionId)
							console.log({...property, options:opts})
							return {
								...property, options:opts
							}
						}
						return property
					})
					return {
						singleAe,properties:prop
					}	
				}
				return singleAe;
			})
		});
	}


	getInputsAccordingToType (property,aeIndex,aePropId,layout,lableLessLayout,ae) {
		switch (property.type){
			case 'int':
			case 'double':
				return (
					<div>
						<FormItem label="Range" {...layout}>
					        <Input.Group size="large">
									<Input
										style={{ width: '25%' }}
										placeholder="minimum"
										name="min"
										value={property.min}
										onChange={this.handleChangeProperty(aeIndex,aePropId,ae)}/>
									<Input
										style={{ width: '25%' }}
										placeholder="maximum"
										name="max"
										value={property.max}
										onChange={this.handleChangeProperty(aeIndex,aePropId,ae)}/>

					        </Input.Group>					
					
						</FormItem>	
					</div>
				)
			case 'discrete':
				const {image, endpoint, name, description, tags, inputVisible, inputValue } = this.state;
				return (
					<div>
					<FormItem label="Options" {...layout} >
						{
							
							(
								<Button
									size="large"
									onClick={this.showInput}
									style={{ background: '#fff', borderStyle: 'dashed',width:'100%' }}
									>
									<Icon type="plus" />Add option
								</Button>
							)
						}
					</FormItem>

					<FormItem {...lableLessLayout} >
						{
							property.options.map((option, optionId) => {
								const h= this.handleClose;
								return  (
									<Tag key={option} closable afterClose={this.handleClose(aeIndex,aePropId,optionId,ae)}>
										{option}
									</Tag>
								);

							})
						}
											
		
						{
							inputVisible && 
							(
								<Input
									ref={this.saveInputRef}
									type="text"
									style={{ width: 78 }}
									name="inputValue"
									onChange={this.onChange}
									onPressEnter={this.handleChangeProperty(aeIndex,aePropId,ae,true)}
									onBlur={this.hideInput}
								/>
							)
						}
						</FormItem>
						</div>							
				)
			default:
		}
	}

	handleRemoveAE = (aeIndex,ae) => () => {
		this.setState({
			[ae]: this.state[ae].filter((singleAe, index) => aeIndex !== index)
		});
	}

	render() {
		const uploadButton = (
			<div>
				<Icon type={this.state.loading ? 'loading' : 'plus'} />
				<div >Upload</div>
			</div>
		);
		const {image, endpoint, name, description, tags, inputVisible, inputValue } = this.state;
	    const formItemLayout = {
	      labelCol: {
	        xs: { span: 24 , offset: 0},
	        sm: { span: 6 , offset: 0},
	      },
	      wrapperCol: {
	        xs: { span: 24 , offset: 0 },
	        sm: { span: 10 , offset: 0},
	      },
	    };
		const formItemLayoutWithOutLabel = {
			wrapperCol: {
			xs: { span: 24, offset: 0 },
			sm: { span: 10, offset: 6 },
			},
		};

		return (
			<ContentWrapper>
					<Row style={{margin:'40px 0'}}>
						<Col  offset={6} >
							<h1>Product creation</h1>
						</Col>
					</Row>

			<Form>


					<FormItem label="Name" {...formItemLayout} >
					  <Input placeholder="Product name" name="name" 
					 	size="large"
						value={name} 
						onChange={this.onChange}/>
					</FormItem>
					<FormItem label="Description" {...formItemLayout}>
					  <TextArea placeholder="Product name" name="description"
					 	 size="large"	 
						value={description} 
						onChange={this.onChange}/>
					</FormItem>

					<FormItem label="Image" {...formItemLayout}>
						<Upload
					        showUploadList={false}
					        action="//jsonplaceholder.typicode.com/posts/"
							name="avatar"
							listType="picture-card"
							showUploadList={false}
							onChange={this.handleChange}
							>
							{image ? <img src={image} alt="" /> : uploadButton}
						</Upload>
					</FormItem>
	

					<FormItem label="Endpoint"  {...formItemLayout}>
					  <Input placeholder="Endpoint" name="endpoint"
					  	size="large" 
						value={endpoint} 
						onChange={this.onChange}/>
					</FormItem>					

				<div>
					<Row style={{margin:'40px 0 10px'}}>
						<Col  offset={6} >
							<h3>Actions</h3>
						</Col>
					</Row>
					{

						this.state.actions.map((action, aeIndex) => (
									<div
										key={action,aeIndex}  
										style={{
										border: '3px solid #ebedf0',
										borderRadius: '10px',
										padding: '20px',
										margin:' 0 0 60px'
									}}>
							<div >
							<FormItem label="Name" {...formItemLayout}>
								<Input
									size="large"
									name="name"
									placeholder={`Action ${aeIndex} name`}
									value={action.name}
									onChange={this.handleChangeAE(aeIndex,'actions')}/>
							</FormItem>
							<FormItem label="Description" {...formItemLayout}>
								<TextArea
									size="large"
									name="description"
									placeholder={`Action ${aeIndex} description`}
									value={action.description}
									onChange={this.handleChangeAE(aeIndex,'actions')}/>
							</FormItem>
												<Row style={{margin:'40px 0 10px'}}>
						<Col  offset={6} >
							<h3>Properties</h3>
						</Col>
					</Row>
								{
									action.properties.map((property,aePropId) => (

									<div
										key={property,aePropId} 
										style={{
										border: '1px solid #ebedf0',
										borderRadius: '2px',
										padding: '20px',
										margin:' 0 0 16px'
									}}>

										<FormItem label="Name" {...formItemLayout}>	
											<Input
												size="large"
												name="name"
												placeholder={`Action ${aeIndex}  Property  ${aePropId} name`}
												value={property.name}
												onChange={this.handleChangeProperty(aeIndex,aePropId,'actions')}/>
										</FormItem>
										<FormItem label="Description" {...formItemLayout}>
											<TextArea
												size="large"
												name="description"
												placeholder={`Action ${aeIndex}  Property  ${aePropId} description`}
												value={property.description}
												onChange={this.handleChangeProperty(aeIndex,aePropId,'actions')}/>
										</FormItem>
										<FormItem label="Endpoint" {...formItemLayout}>	
											<Input
												size="large"
												name="endpoint"
												placeholder={`Action ${aeIndex}  Property  ${aePropId} name`}
												value={property.endpoint}
												onChange={this.handleChangeProperty(aeIndex,aePropId,'actions')}/>
										</FormItem>		
										<FormItem label="Type" {...formItemLayout}>
											<Radio.Group value={property.type} name="type" onChange={this.handleChangeProperty(aeIndex,aePropId,'actions')}>
												<Radio.Button value="string">String</Radio.Button>
												<Radio.Button value="int">Int</Radio.Button>
												<Radio.Button value="double">Double</Radio.Button>
												<Radio.Button value="discrete">Discrete</Radio.Button>
											</Radio.Group>
										</FormItem>
											{ this.getInputsAccordingToType(property,aeIndex,aePropId,formItemLayout,formItemLayoutWithOutLabel,'actions') }

										<FormItem {...formItemLayoutWithOutLabel}>
											<Button onClick={this.handleRemoveProperty(aeIndex,aePropId,'actions')} size="large"  style={{width:'100%' }}>
												<Icon type="minus" />Remove property
											</Button>
										</FormItem>

									</div>
		
									))
								}
		
								<FormItem {...formItemLayoutWithOutLabel}>
									<Button type="dashed" onClick={this.handleAddProperty(aeIndex,'actions')} size="large"  style={{width:'100%' }}>
										<Icon type="plus" />Add property
									</Button>
								</FormItem>
								<FormItem {...formItemLayoutWithOutLabel}>
									<Button onClick={this.handleRemoveAE(aeIndex,'events')}  size="large" style={{width:'100%' }}>
										<Icon type="minus" />Remove action
									</Button>
								</FormItem>
							</div>
							</div>
						))
					}
					<FormItem {...formItemLayoutWithOutLabel} >
						<Button type="dashed" size="large" height={200} onClick={this.handleAddAE('actions')}  style={{width:'100%' }}>
						<Icon type="plus" />Add action
						</Button>
					</FormItem>
				</div>
			</Form>	

			</ContentWrapper>
		);
  }
}

export default connect(null, null)(ProductsPage);