<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>Enduser</title>
	<link href="https://fonts.googleapis.com/css?family=Open+Sans" rel="stylesheet">
	<link href="https://fonts.googleapis.com/css?family=Karla:400,700|Montserrat:700" rel="stylesheet">
	<link href="https://fonts.googleapis.com/css?family=Roboto:400,700" rel="stylesheet">
	<link rel="stylesheet" href="style.css">

</head>
<body>
	<div class="container">
		<header>
			<nav class="main-nav">
				<ul class="main-nav__menu left" >
					<li class="main-nav__item is-active">
						<a href="">Devices</a>
						<span class="main-nav__count">5</span>
					</li>
					<li class="main-nav__item">
						<a href="">Scenarios</a>
						<span class="main-nav__count">3</span>
					</li>

				</ul>
			</nav>
		</header>
		
		<div class="">
			<button  id="add_device" class=" button--blue button--small">Add device</button>
			<ul class="devices">
				<li class="devices__item ">
					<div class="devices__img IMG_1">		
					</div>
					<div class="devices__name">
						<a class="" href="">Smart Watch</a>
					</div>
				</li>

				<li class="devices__item ">
					<div class="devices__img IMG_3">		
					</div>
					<div class="devices__name">
						<a class="" href="">Anal bids</a>
					</div>
				</li>

				<li class="devices__item ">
					<div class="devices__img IMG_2">		
					</div>
					<div class="devices__name">
						<a class="" href="">Web Cam</a>
					</div>
				</li>
			</ul>		
		</div>


			<div class="modal is-hidden">
				<form action="signup" method="get" class="form modal__content">
					<h3 class="form__header">Add device</h1>
					<div class="input">
						<label class="input__label full-width"> Vendor</label>
						<select class="full-width" name="vendor">
							<option>Cow</option>
							<option>Horse</option>
							<option>Kangaroo</option>
							<option>Rabbit</option>
							<option>Duck</option>
							<option>Mouse</option>
							<option>Fish</option>
							<option>Bird</option>
							<option>Elephant</option>
							<option>Ape</option>
						</select>				
					</div>

					<div class="input">
						<label class="input__label full-width"> Product</label>
						<select class="full-width" disabled name="product">
							<option disabled selected value> Choose vendor first</option>
							<option>Fish</option>
							<option>Bird</option>
							<option>Elephant</option>
							<option>Ape</option>
						</select>				
					</div>
					<div class="input">
						<label class="input__label full-width">Device ID </label>
						<input type="text" align="right" class="full-width" placeholder="Device ID" name="deviceid" >
					</div>
					<button type="submit" class="input button--blue full-width" >Add device</button>
				</form>	
			</div>

	</div>
<script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
<script src="script.js"></script>
</body>
</html>