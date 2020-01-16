package nl.ycn.coaching.controller;


import nl.ycn.coaching.database.*;
import nl.ycn.coaching.model.Bootcamp;
import nl.ycn.coaching.model.Course;
import nl.ycn.coaching.model.CourseCreationDto;
import nl.ycn.coaching.model.users.AppUser;
import nl.ycn.coaching.model.users.Trainee;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
public class HrController {

	@Autowired
	private AppUserService appUserService;

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private BootcampRepository bootcampRepository;

	@Autowired
	private BootcampService bootcampService;

	@Autowired
	private HrService hrService;

	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	//Mappings for HR-Employee
	@GetMapping("hremployee/bootcamps")
	public String getBootcamps(Model model) {
		try {
			AppUser user = appUserService.getActiveUser();
			String role = user.getRole();
			model.addAttribute("activeBootcamps", hrService.getTopActiveBootcamps(100));
			model.addAttribute("finishedBootcamps", hrService.getTopFinishedBootcamps(100));

			return role.toLowerCase() + "/bootcamps";
		} catch (Exception e) {
			return "/login";
		}
	}

	@GetMapping("/hremployee/editbootcamp/{bootcampName}")
	public String getEditBootcampPage(Model model, @PathVariable("bootcampName") String bootcampName) {
		model.addAttribute("bootcamp", bootcampRepository.findByBootcampName(bootcampName));
		CourseCreationDto c = new CourseCreationDto(bootcampService.getCourseList(bootcampName));
		model.addAttribute("coursesdto", c);
		model.addAttribute("allcourses", courseRepository.findAll());
		return "/hremployee/editbootcamp";
	}

	@PostMapping("/hremployee/updatebootcamp/{bootcampName}")
	public String updateCourse(@PathVariable("bootcampName") String bootcampName,
							CourseCreationDto courseCreationDto,
							@RequestParam(value="action", required = false) String action,
							@RequestParam(value="bootcampname") String name,
							@RequestParam(value="bootcampbegindate") String beginDate,
							@RequestParam(value="bootcampenddate") String endDate){
		Bootcamp camp = bootcampRepository.findByBootcampName(bootcampName);


		if (action.equals("add_course")){
			bootcampService.addCourse(courseCreationDto, camp);

			return "redirect:/hremployee/editbootcamp/{bootcampName}";
		} else if (action.equals("delete_course")){
			bootcampService.deleteCourse(courseCreationDto, camp);
			return "redirect:/hremployee/editbootcamp/{bootcampName}";
		} else if (action.equals("save_bootcamp")){
			bootcampService.saveBootcamp(courseCreationDto, camp, name, beginDate, endDate);
			return "redirect:/hremployee/bootcamps";
		} else {
			return "redirect:/hremployee/bootcamps";
		}

	}

	@GetMapping("/hremployee/addbootcamp")
	public String addBootcamp(Model model){
		model.addAttribute("bootcampName", "");
		CourseCreationDto c = new CourseCreationDto();
		model.addAttribute("coursesdto", c);
		model.addAttribute("courseList", courseRepository.findAll());
		return "/hremployee/createbootcamp";
	}


	@GetMapping("/hremployee/deletecourse/{bootcamp}/{course}")
	public String deleteCourse(@PathVariable("bootcamp") String bootcamp, @PathVariable("course") String course) {
		return "redirect:/hremployee/editbootcamp/{bootcamp}";
	}

	//postmapping to change the state of the bootcamp active <--> inactive
	@PostMapping(path = "/hremployee/setactivationbootcamp/{bootcamp}")
	public String setActivationBootcamp(Model model, @PathVariable("bootcamp") String bootcamp) {
		Bootcamp disBootcamp = bootcampRepository.findByBootcampName(bootcamp);
		boolean active = disBootcamp.getActive();
		disBootcamp.setActive(!active);
		bootcampRepository.save(disBootcamp);
		return "redirect:/hremployee/bootcamps";
	}

	@GetMapping({"/hremployee/dashboard","/hremployee"})
	public String getHrDashboard(Model model) {
		model.addAttribute("activeBootcamps", hrService.getTopActiveBootcamps(5));
		model.addAttribute("activeBootcamps", hrService.getTopFinishedBootcamps(5));

		return "/hremployee/hrdashboard";
	}

	@GetMapping(path = "/hremployee/appuserinfo/{username}")
	public String getAppUserInfo(Model model, @PathVariable("username") String username) {

		model.addAttribute("appuser", appUserService.getUser(username));
		model.addAttribute("bootcampList", bootcampRepository.findAll());

		if (appUserService.getUser(username).getRole().equalsIgnoreCase("trainee")) {
			AppUser user = appUserService.getUser(username);
			Trainee trainee = (Trainee) user;
			model.addAttribute("bootcamp", trainee.getBootcamp().getName());
		}
		return "/hremployee/appuserinfo";
	}

	@PostMapping(path = "hremployee/updateappuserinfo/{username}")
	public String updateAppUserInfo(@PathVariable("username") String username,
									String firstname,
									String lastname,
									String email,
									String roles,
									String password,
									boolean enabled,
									boolean activated,
									Date dateofbirth,
									String zipcode,
									String street,
									String streetNr,
									String city,
									String country,
									String telephonenumber,
									String bootcamp) {
		appUserService.updateAppUser(username,
				firstname,
				lastname,
				email,
				password,
				enabled,
				activated,
				dateofbirth,
				zipcode,
				street,
				streetNr,
				city,
				country,
				telephonenumber,
				bootcamp);
		return "redirect:/hremployee/users";
	}


	@GetMapping("/hremployee/users")
	public String getUsers(Model model) {
		try {
			AppUser user = appUserService.getActiveUser();
			String role = user.getRole();
			model.addAttribute("hremployees", appUserService.getAppUsersByRole("HREMPLOYEE"));
			model.addAttribute("trainees", appUserService.getAppUsersByRole("TRAINEE"));
			model.addAttribute("managers", appUserService.getAppUsersByRole("MANAGER"));
			model.addAttribute("talentmanagers", appUserService.getAppUsersByRole("TALENTMANAGER"));
			return "/" + role.toLowerCase() + "/users";
		} catch (Exception e) {
			return "/login";
		}
	}

	@GetMapping("/hremployee/teams")
	public String getTeams() {
		try {
			AppUser user = appUserService.getActiveUser();
			String role = user.getRole();
			return role.toLowerCase() + "/teams";
		} catch (Exception e) {
			return "/login";
		}
	}

	@GetMapping("/hremployee/skills")
	public String getSkills() {
		try {
			AppUser user = appUserService.getActiveUser();
			String role = user.getRole();
			return role.toLowerCase() + "/skills";
		} catch (Exception e) {
			return "/login";
		}
	}

	@GetMapping("/hremployee/createsoftskillform")
	public String getSoftskillForm() {
		try {
			AppUser user = appUserService.getActiveUser();
			String role = user.getRole();
			return role.toLowerCase() + "/createsoftskillform";
		} catch (Exception e) {
			return "/login";
		}
	}

	@GetMapping("/hremployee/register")
	public String register(Model model) {

		try {
			AppUser user = appUserService.getActiveUser();
			String role = user.getRole();

			String upperCaseLetters = RandomStringUtils.random(3, 65, 90, true, true);
			String lowerCaseLetters = RandomStringUtils.random(3, 97, 122, true, true);
			String numbers = RandomStringUtils.randomNumeric(3);
			String specialChar = RandomStringUtils.random(3, 33, 47, false, false);
			String totalChars = RandomStringUtils.randomAlphanumeric(3);
			String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
					.concat(numbers)
					.concat(specialChar)
					.concat(totalChars);
			List<Character> pwdChars = combinedChars.chars()
					.mapToObj(c -> (char) c)
					.collect(Collectors.toList());
			Collections.shuffle(pwdChars);
			String password = pwdChars.stream()
					.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
					.toString();
			model.addAttribute("randomPassword", password);
			return role.toLowerCase() + "/register";
		} catch (Exception e) {
			return "/login";
		}
	}

	@GetMapping("/hremployee/hrcalendar")
	public String getCalendar() {
		return "/hremployee/hrcalendar";
	}

	//register a new AppUser (from hremployee/register)
	@PostMapping("/hremployee/addappuser")
	public String register(String username,
						   String firstname,
						   String lastname,
						   String email,
						   String password,
						   String roles,
						   boolean enabled,
						   boolean activated) {


		appUserService.registerUser(
				username,
				firstname,
				lastname,
				email,
				encoder.encode(password),
				roles,
				enabled,
				activated);
		return "redirect:/hremployee/users";
	}

	@PostMapping("/hremployee/createsoftskill")
	public String createSoftskill(String name, String description) {
		hrService.addSoftskill(name, description);

		return "/hremployee/hrdashboard";
	}


}
