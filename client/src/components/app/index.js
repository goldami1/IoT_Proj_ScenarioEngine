import React, { PureComponent } from "react";
import { Link, withRouter } from "react-router-dom";
import { connect } from "react-redux";
import {
	Avatar,
	Popconfirm,
	Popover,
	Layout,
	Menu,
	Icon,
	Dropdown,
	Button,
	message
} from "antd";
import Navigation from "./Navigation";
import UserMenu from "./UserMenu";
import links from "./links";
import userLinks from "./userLinks";
import { logout } from "../../actions/auth_actions";
const { Header, Sider, Content, Footer } = Layout;

class App extends PureComponent {
	constructor(props) {
		super(props);
		this.state = {
			collapsed: false
		};
	}

	onCollapse = collapsed => {
		this.setState({ collapsed });
	};

	showMessage = () => {
		const { content, type } = this.props.message;
		if (content) {
			message[type](content);
		}
	};

	logout = () =>{ this.props.logout(this.props.history); }
		
	
	render() {
		const { auth, location } = this.props;
		
		if(this.props.message)
		{
			this.showMessage();
		}

		const userMenu = 
			(
				<Menu>
					<Menu.Item>Menu</Menu.Item>
					<Menu.SubMenu title="SubMenu">
					<Menu.Item>SubMenuItem</Menu.Item>
					</Menu.SubMenu>
			</Menu>)
		;
		return (
			<Layout style={{ minHeight: "100vh" }}>
				<Sider
					collapsible
					collapsed={this.state.collapsed}
					onCollapse={this.onCollapse}
					style={{ position: "relative", overflow: "hidden",paddingTop: 60}}	
				>
					<Navigation links={links(auth)} selected={location.pathname} />	
					<UserMenu logout={this.logout} links={userLinks(auth)}/>
				</Sider>
				<Layout style={{ height: "100vh" }}>
					<Content>{this.props.children}</Content>
					<Footer style={{ textAlign: "center" }}>
						Scenario Engine ©2018 Created by Gil
					</Footer>
				</Layout>
			</Layout>
		);
	}
}

function mapStateToProps({ auth, app }) {
	return {
		auth,
		message: app
	};
}

export default connect(mapStateToProps, { logout })(withRouter(App));
