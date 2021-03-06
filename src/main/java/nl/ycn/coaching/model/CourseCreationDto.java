package nl.ycn.coaching.model;

import java.util.ArrayList;
import java.util.List;

public class CourseCreationDto {
	List<Course> courses;

	public CourseCreationDto(){
		this.courses = new ArrayList<>();
	}
	public CourseCreationDto(List<Course> courses){
		this.courses = courses;
	}

	public List<Course> getCourses() {
		return courses;
	}

	public void setCourses(List<Course> courses) {
		this.courses = courses;
	}

	public void addCourses(Course course){
		this.courses.add(course);
	}
}
