package com.bel.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bel.model.Customer;
import com.bel.model.Product;
import com.bel.repository.CustomerRepository;
import com.bel.service.CustomerService;
import com.bel.service.ProductConsumer;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


// hit URL http://localhost:6002/customer to get a product via Customer service
// hit URL 
@RestController
@RequestMapping("/customer")
public class CustomerController {

	@Autowired
	private CustomerService cservice;
	
	
	@Autowired
	private CustomerRepository crepo;
	
	@Autowired
	private ProductConsumer consumer;
	
	
	@GetMapping(value = "/get/{id}")
	public ResponseEntity<Customer> getProduct(@PathVariable("id") Long pId){
		return new ResponseEntity<>(cservice.getCustomer(pId), HttpStatus.OK);
	}



	@PostMapping
	public ResponseEntity<List<Customer>> addNewCustomer(@RequestBody Customer customer){
		Product p = customer.getProduct();
		customer.setProduct(p);
		p.setCustomer(customer);
		//consumer.addNewProduct(p);				//// Calling Parent
	return new ResponseEntity<>(cservice.saveCustomer(customer), HttpStatus.CREATED);
	}
	
	
	@PutMapping("/update/{id}")
	public ResponseEntity<Customer> updateCustomer(@PathVariable("id") Long pId,@RequestBody Customer customer){
		Customer c = crepo.findById(pId).get();
		//Fetching Product ID to update in product microservice
		Product pro = c.getProduct();
		
		c.setCustName(customer.getCustName());
		
		pro.setProductName(customer.getProduct().getProductName());
		pro.setPrice(customer.getProduct().getPrice());
		
		c.setProduct(pro);
		pro.setCustomer(c);
		

	//	Product uProduct = consumer.updateProduct(pro.getProductId(), pro);			// Calling Parent
	//	uProduct.setCustomer(c);
		Customer updatedCustomer = cservice.updateCustomer(pId, c);
		return ResponseEntity.ok().body(updatedCustomer);
	}
	
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteCustomer(@PathVariable("id") Long pId){
		//Product p = cservice.getCustomer(pId).getProduct();
		cservice.deleteCustomer(pId);
		//consumer.deleteProduct(p.getProductId());				//Calling Parent
		return ResponseEntity.ok().body("Customer Deleted.");
	}
	
	@HystrixCommand(fallbackMethod = "callServiceAndGetData_Fallback")
	@GetMapping
	public ResponseEntity<List<Product>> getProducts(){
		List<Product> p = consumer.getProducts();
		System.out.println("######################"+p.toString());
		return ResponseEntity.ok().body(p);
	}
	
	public ResponseEntity<String> callServiceAndGetData_Fallback() {
		System.out.println("Product Service is down!!! fallback route enabled...");
		return ResponseEntity.ok().body("CIRCUIT BREAKER ENABLED!!! No Response From Product Service at this moment. Product Service is down!!! " +
		" Service will be back shortly - ");
		}
	@PostMapping("/save")
	public ResponseEntity<List<Product>> saveProduct(@RequestBody Product p){
		List<Product> pNew = consumer.addNewProduct(p);
		System.out.println("######################"+p.toString());
		return ResponseEntity.ok().body(pNew);
	}
	
	/*@GetMapping(value = "/getproduct/{id}")
	public ResponseEntity<Product> getProduct1(@PathVariable("id") Long pId){
		return new ResponseEntity<>(consumer.getProduct(pId), HttpStatus.OK);
	}
	

	@PutMapping("/updateproduct/{id}")
	public ResponseEntity<Product> updateProduct(@PathVariable("id") Long pId,@RequestBody Product pro){
		Product pNew = consumer.updateProduct(pId,pro);
		System.out.println("######################"+pNew.toString());
		return ResponseEntity.ok().body(pNew);
	}

	@DeleteMapping("/deleteproduct/{id}")
	public ResponseEntity<String> deleteProduct(@PathVariable("id") Long pId){
		consumer.deleteProduct(pId);
	return ResponseEntity.ok().body("Product Deleted.");
	}*/
	
}
