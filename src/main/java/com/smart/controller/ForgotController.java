package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRespository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class ForgotController {

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRespository userRespository;
	
	@Autowired
	private BCryptPasswordEncoder encoder;

	Random random = new Random(1000);

	@GetMapping("/forgot")
	public String forgotpassword(Model m) {
		m.addAttribute("title", "Forgot Password-Smart Contact Manager");
		return "forgot_password";
	}

	@PostMapping("/send_otp")
	public String verifyOTP(Model m, @RequestParam("email") String email, HttpSession session) {

		m.addAttribute("title", "Send OTP-Smart Contact Manager");

		// generating otp
		int otp = random.nextInt(9999);

		// sending otp to email
		String subject = "Smart Contact Manager OTP";
		String body = "OTP send for verifying your email address." + "<div>" + "OTP : " + "<h3><b>" + otp + "</b></h3>"
				+ "</div>";
		boolean mail = emailService.sendMail(email, subject, body);

		System.out.println(mail);

		if (mail) {
			session.setAttribute("System-otp", otp);
			session.setAttribute("email", email);
			return "send_otp";
		} else {
			session.setAttribute("message", new Message("please check your Email ID", "danger"));
			return "forgot_password";
		}

	}

	@PostMapping("/verify_otp")
	public String verifyOTP(@RequestParam("otp") int otp, HttpSession session) {

		int systemOtp = (int) session.getAttribute("System-otp");
		String emailId = (String) session.getAttribute("email");

		if (systemOtp == otp) {

			// check email in DB
			User userbyEmail = userRespository.getUserbyEmail(emailId);
			if (userbyEmail != null) {
				// go to change password page
				return "change_password";
			} else {
				session.setAttribute("message", new Message("User does not exists with this mail Id !!", "danger"));
				return "forgot_password";
			}

		} else {

			// send message wrong otp
			session.setAttribute("message", new Message("wrong OTP entered !!", "danger"));
			// go to forgot password page
			return "send_otp";
		}

	}
	
	@PostMapping("/change_password")
	public String changePassword(@RequestParam("newPassword") String password,HttpSession session) {
		
		String emailId = (String) session.getAttribute("email");
		User user = userRespository.getUserbyEmail(emailId);
		
		user.setPassword(encoder.encode(password));
		userRespository.save(user);
		
		return "redirect:/login?change=Password Changed Successfully...";
	}

}
