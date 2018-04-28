import { getFromApi } from "./appActions";


// const DEBUG = true;

export const endpoints = {
    addCustomer : "addCustomer",
    addVendor   : "addVendor",
    device      : "device",
    vendor      : "vendor",
    vendors     : "vendors",
    scenario    : "scenario",
    login       : "login",
    product     : "product"
}

const API_ = {
    // ip          : "192.168.1.12",
    ip          : "localhost",
    port        : "8080",
    folder      : "Scenario_Engine/webapi",   
}
const API = {
    login       : `http://${API_.ip}:${API_.port}/${API_.folder}/customer`,
    vendor      : `http://${API_.ip}:${API_.port}/${API_.folder}/vendor`,
    customer    : `http://${API_.ip}:${API_.port}/${API_.folder}/customer`,
    addCustomer : `http://${API_.ip}:${API_.port}/${API_.folder}/customer/new`,
    addVendor   : `http://${API_.ip}:${API_.port}/${API_.folder}/vendor/new`,
    device     : `http://${API_.ip}:${API_.port}/${API_.folder}/customer/device`,
    vendors     : `http://${API_.ip}:${API_.port}/${API_.folder}/customer/vendors`,
    product    : `http://${API_.ip}:${API_.port}/${API_.folder}/vendor/product`,
    scenario   : `http://${API_.ip}:${API_.port}/${API_.folder}/customer/scenario/`
}


const _API_ = {
    ip          : "localhost",
    port        : "8086",
    folder      : "Scenario_Engine/webapi",   
}
const _API = {
    login       : `http://${_API_.ip}:${_API_.port}/${_API_.folder}/customer`,
    vendor      : `http://${_API_.ip}:${_API_.port}/${_API_.folder}/vendor`,
    customer    : `http://${_API_.ip}:${_API_.port}/${_API_.folder}/customer`,
    addCustomer : `http://${_API_.ip}:${_API_.port}/${_API_.folder}/customer/new`,
    addVendor   : `http://${_API_.ip}:${_API_.port}/${_API_.folder}/vendor/new`,
    device     : `http://${_API_.ip}:${_API_.port}/${_API_.folder}/customer/device`,
    vendors     : `http://${_API_.ip}:${_API_.port}/${_API_.folder}/customer/vendors`,
    product    : `http://${_API_.ip}:${_API_.port}/${_API_.folder}/vendor/product`,
    scenario   : `http://${_API_.ip}:${_API_.port}/${_API_.folder}/customer/scenario/`
}

export function api(endpoint) {
    const api = ( typeof DEBUG === 'undefined' ) ? API : _API;
    return api[endpoint];
}

const address   = "localhost";
const port      = "8080";
const folder    = "Scenario_Engine/webapi";

const base 		= `http://${address}:${port}/${folder}`;
const customer 	= `${base}/customer`;
const vendor 	= `${base}/vendor`;

export const REST_LOGIN 	    = customer;
// export const addCustomer 	    = `${customer}/new`;
export const addvendor 		    = `${vendor}/new`;
export const REST_DEVICES 	    = `${customer}/device`;
export const REST_VENDORS 	    = `${customer}/vendors`;
export const REST_PRODUCTS      = `${vendor}/product`;

export const scenario 		    = `${customer}/scenario/`;
export const addScenario        = `${scenario}` 


// user  = {
//     "customer": true,
//     "email": "romadym1@gmail.com",
//     "id": 13,
//     "name": "Roma Dyment Roma Dyment",
//     "password": "Aa123456",
//     "userName": "romadym1",
//     "customerScenarios": [],
//     "devices": []
// }

// device =    {
//     "customer_id": 13,
//     "id": 817,
//     "protoDevice": {},
//     "serial_number": 5
// }

// vendor =  {
//     "id": 7,
//     "name": "LG"
// }

// product = {
//     "product_id": 252,
//     "product_name": "product name"
// }

// product = {
//     "actionAndEventList": [
//         {
//             "description": "fasd",
//             "id": 797,
//             "isEvent": false,
//             "max": [
//                 "null",
//                 "44"
//             ],
//             "min": [
//                 "null",
//                 "2"
//             ],
//             "name": "asdfasd",
//             "paramDesc": [
//                 "adsfads",
//                 "asdf"
//             ],
//             "prodId": 796,
//             "productEP": "fadsf",
//             "supportedParametersName": [],
//             "supportedValues":[
//                 ["asd","asd"],
//                 []
//             ]
//             "types": [
//                 "discrete",
//                 "int"
//             ]
//         }
//     ],
//     "actionState": true,
//     "description": "asdf",
//     "endPoint": "asdf",
//     "eventState": true,
//     "id": 796,
//     "name": "asdf",
//     "vendorName": "Ami Ami",
//     "vendor_id": 7
// }