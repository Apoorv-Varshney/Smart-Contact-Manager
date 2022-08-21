package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.smart.dao.UserRespository;
import com.smart.entities.User;

public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRespository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User userbyEmail = userRepository.getUserbyEmail(username);
		if(userbyEmail==null) {
			throw new UsernameNotFoundException("Could not found user !!");
		}
		CustomUserDetails customUserDetails = new CustomUserDetails(userbyEmail);
		return customUserDetails;
	}

}
