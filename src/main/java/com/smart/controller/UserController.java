package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRespository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRespository userRespository;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@ModelAttribute
	public void commonUserData(Model m, Principal principal) {
		String email = principal.getName();
		User user = userRespository.getUserbyEmail(email);
		m.addAttribute("user", user);
	}

	@GetMapping("/index")
	public String dashboard(Model m, Principal principal) {
		m.addAttribute("title", "User Home-Smart Contact Manager");
		return "user/user_dashboard";
	}

	@GetMapping("/addcontact")
	public String addContact(Model m) {
		m.addAttribute("title", "Add Contact-Smart Contact Manager");
		m.addAttribute("contact", new Contact());
		return "user/user_addcontact";
	}

	@PostMapping("/process_contact")
	public String processContact(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {

			String email = principal.getName();
			User user = userRespository.getUserbyEmail(email);

			if (file.isEmpty()) {

				// setting default profile image
				contact.setImage("contact.png");

				System.out.println("Please choose Image File");

			} else {

				// saving filename to DB
				contact.setImage(file.getOriginalFilename());

				// storing file in folder
				File saveFile = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is Uploaded..");
			}

			// set user and contacts to each other
			contact.setUser(user);
			user.getContacts().add(contact);

			// update user in DB
			userRespository.save(user);

			// success message
			session.setAttribute("message", new Message("Successfully Added..Please add More !!", "success"));

		} catch (Exception e) {

			System.out.println(e.getMessage());

			// error message
			session.setAttribute("message", new Message("Something went wrong.." + e.getMessage(), "danger"));
		}
		System.out.println("user and contact saved...");
		return "user/user_addcontact";
	}

	@GetMapping("/view_contact/{page}")
	public String viewContact(@PathVariable("page") int page, Model m, Principal principal) {
		m.addAttribute("title", "View Contact-Smart Contact Manager");

		// fetching email of current user
		String email = principal.getName();

		// getting user using email
		User user = userRespository.getUserbyEmail(email);

		// setting currentPage and records/page
		Pageable pageable = PageRequest.of(page, 4);

		// getting list of contacts
		Page<Contact> contactByUser = contactRepository.getContactByUser(user.getId(), pageable);

		// adding pagination values and contact list
		m.addAttribute("contacts", contactByUser);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPage", contactByUser.getTotalPages());

		return "user/user_viewcontact";
	}

	@GetMapping("/contact/{id}")
	public String specificContact(@PathVariable("id") int id, Model m, Principal principal) {

		m.addAttribute("title", "Contact Details-Smart Contact Manager");

		// getting user based on email
		String email = principal.getName();
		User user = userRespository.getUserbyEmail(email);

		// getting contactDetails using id
		Optional<Contact> findById = contactRepository.findById(id);
		Contact contactDetail = findById.get();

		// sending it only if contacts are of current user
		if (user.getId() == contactDetail.getUser().getId()) {
			m.addAttribute("contactDetail", contactDetail);
		}

		return "user/user_contact";
	}

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") int cid, Model m, Principal principal, HttpSession session) {

		Optional<Contact> findById = contactRepository.findById(cid);
		Contact contact = findById.get();

		// getting user based on email
		String email = principal.getName();
		User user = userRespository.getUserbyEmail(email);

		// delete user and image
		if (user.getId() == contact.getUser().getId()) {

			try {

				// delete image file
				File deleteFile = new ClassPathResource("static/images").getFile();
				File file1 = new File(deleteFile, contact.getImage());
				file1.delete();

				// delete contact
				contactRepository.delete(contact);
				session.setAttribute("message", new Message("Contact deleted successfully..", "success"));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return "redirect:/user/view_contact/0";
	}

	@PostMapping("/update/{cid}")
	public String updateContact(@PathVariable("cid") int cid, Model m) {

		Optional<Contact> findById = contactRepository.findById(cid);
		Contact contact = findById.get();

		m.addAttribute("title", "Update Contact-Smart Contact Manager");
		m.addAttribute("contact", contact);

		return "user/user_updatecontact";
	}

	@PostMapping("/process_update")
	public String processUpdate(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {

			Contact oldContact = contactRepository.findById(contact.getCid()).get();

			if (!file.isEmpty()) {

				// delete old file
				File deleteFile = new ClassPathResource("static/images").getFile();
				File file1 = new File(deleteFile, oldContact.getImage());
				file1.delete();

				// update new file
				File saveFile = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());

			} else {

				// save old file
				contact.setImage(oldContact.getImage());
			}

			// getting user based on email
			String email = principal.getName();
			User user = userRespository.getUserbyEmail(email);

			// updating user
			contact.setUser(user);
			contactRepository.save(contact);

			session.setAttribute("message", new Message("Contact Updated successfully..", "success"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(contact.getName());
		System.out.println(contact.getCid());
		return "redirect:/user/view_contact/0";
	}

	@GetMapping("/profile")
	public String profile(Model m) {

		m.addAttribute("title", "Profile-Smart Contact Manager");
		return "user/user_profile";
	}

	@GetMapping("/user_delete/{id}")
	public String deleteUser(@PathVariable("id") int id, Model m, Principal principal) {

		Optional<User> findById = userRespository.findById(id);
		User user = findById.get();

		userRespository.delete(user);
		System.out.println("User deleted");

		return "redirect:/logout";
	}

	@PostMapping("/user_update/{id}")
	public String updateUser(@PathVariable("id") int id, Model m) {
		m.addAttribute("title", "Update User-Smart Contact Manager");
		return "user/user_updateuser";
	}

	@PostMapping("/process_update_user")
	public String processUserUpdate(@ModelAttribute User user, HttpSession session) {

		System.out.println("USER got" + user);
		// set old image
		user.setImage("default.png");
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// updating user
		userRespository.save(user);
		System.out.println("user updated" + user);
		session.setAttribute("message", new Message("User Updated successfully..", "success"));

		return "redirect:/user/profile";
	}

	@GetMapping("/settings")
	public String settings(Model m) {
		m.addAttribute("title", "Settings-Smart Contact Manager");
		return "user/user_settings";
	}

	@PostMapping("/password_change")
	public String passwordChange(@RequestParam("old") String Old, @RequestParam("new") String New, Principal principal,
			HttpSession session) {

		System.out.println(Old);
		System.out.println(New);
		
		String name = principal.getName();
		User userbyEmail = userRespository.getUserbyEmail(name);
		
		if(passwordEncoder.matches(Old, userbyEmail.getPassword())) {
			
			//setting new password after encoding it
			userbyEmail.setPassword(passwordEncoder.encode(New));
			userRespository.save(userbyEmail);
			
			session.setAttribute("message", new Message("Password changed Successfully...", "success"));
			
		} else {
			
			//error
			session.setAttribute("message", new Message("Please provide correct password...", "danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
}
