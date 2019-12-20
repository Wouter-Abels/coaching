package nl.ycn.coaching.controller;

import nl.ycn.coaching.model.PersonalEducationPlan;
import nl.ycn.coaching.model.PersonalHardskill;
import nl.ycn.coaching.model.users.Trainee;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Date;
import java.util.List;

@Controller
public class PepController {

	@PostMapping("personaleducationplanpage")
	public String goToPePpage(){
		return "/dashboardpages/personaleducationplanpage";
	}

	@GetMapping("personalhardskillform")
	public String getpersonalhardskillform(){
		return "/dashboardpages/personalhardskillform";
	}

	@PostMapping("openhardskillform")
	public String openHardskillForm(){
		return "personalhardskillform";
	}

	@PostMapping("createpersonalhardskill")
	public String createPersonalHardskill(String name, String description, Date start, Date end){

		PersonalHardskill skill = new PersonalHardskill(name, description, start, end);
		Trainee trainee = null;
		PersonalEducationPlan pep = trainee.getPepPlan();
		pep.addHardskill(skill);

		return "/dashboardpages/personaleducationplanpage";
	}
}
