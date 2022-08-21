package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRespository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRespository userRespository;

	@GetMapping("/")
	public String homeHandler(Model m) {
		m.addAttribute("title", "Home-Smart Contact Manager");
		return "home";
	}
	
	@GetMapping("/login")
	public String loginHandler(Model m) {
		m.addAttribute("title", "Login-Smart Contact Manager");
		return "login";
	}

	@GetMapping("/about")
	public String aboutHandler(Model m) {
		m.addAttribute("title", "About-Smart Contact Manager");
		return "about";
	}

	@GetMapping("/signup")
	public String signupHandler(Model m) {
		m.addAttribute("title", "Register-Smart Contact Manager");
		m.addAttribute("user", new User());
		return "signup";
	}

	@PostMapping("/register")
	public String registerHandler(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model m,
			HttpSession session) {

		try {
			if (!agreement) {
				System.out.println("please accept terms and conditions...");
				throw new Exception("please accept terms and conditions...");
			}

			System.out.println("error : "+result);
			
			if (result.hasErrors()) {
				m.addAttribute("user", user);
				System.out.println("Errors" + result.getFieldErrors().toString());
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImage("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			User save = userRespository.save(user);
			m.addAttribute("user", save);
			System.out.println("USER" + save);

			m.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered...", "alert-success"));

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong : " + e.getMessage(), "alert-danger"));
		}
		return "signup";
	}
}
