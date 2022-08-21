package com.smart.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRespository;
import com.smart.entities.Contact;
import com.smart.entities.User;

@RestController
public class SearchController {

	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private UserRespository userRespository;
	
	@GetMapping("/search/{query}")
	public ResponseEntity<?> searchFunction(@PathVariable("query") String query,Principal principal) {
		
		//fetching user by email 
		String email = principal.getName();
		User userbyEmail = userRespository.getUserbyEmail(email);
		
		//filtering contacts based on query and particular user
		List<Contact> contacts = contactRepository.findByNameContainingAndUser(query, userbyEmail);
		
		return ResponseEntity.ok(contacts);
	}
	
}
