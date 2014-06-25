package com.bearprogrammer.blog.sample.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import com.bearprogrammer.blog.sample.ToDo;
import com.bearprogrammer.blog.sample.repository.ToDoRepository;

public class TestToDoController {
	
	ToDoRepository toDoRepository;
	ToDoController subjectUnderTest;
	
	@Before
	public void setup() {
		toDoRepository = Mockito.mock(ToDoRepository.class);
		
		subjectUnderTest = new ToDoController();
		subjectUnderTest.toDoRepository = toDoRepository;
	}
	
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testUserIsInRole() {
		String expectedRole = "test";
		UserDetails mockUser = Mockito.mock(UserDetails.class);
		
		SimpleGrantedAuthority auth = new SimpleGrantedAuthority(expectedRole);
		List auths = new ArrayList();
		auths.add(auth);
		
		Mockito.when(mockUser.getAuthorities()).thenReturn(auths);
		
		boolean result = subjectUnderTest.isUserInRole(mockUser, expectedRole);
		Assert.assertTrue("Should find user in role.", result);
	}
	
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testUserNotInRole() {
		String expectedRole = "test";
		UserDetails mockUser = Mockito.mock(UserDetails.class);
		
		SimpleGrantedAuthority auth = new SimpleGrantedAuthority("somethingElse");
		List auths = new ArrayList();
		auths.add(auth);
		
		Mockito.when(mockUser.getAuthorities()).thenReturn(auths);
		
		boolean result = subjectUnderTest.isUserInRole(mockUser, expectedRole);
		Assert.assertFalse("Should not find user in role.", result);
	}
	
	@Test
	public void testCreate() {
		subjectUnderTest = Mockito.spy(subjectUnderTest);
		
		ModelAndView mav = new ModelAndView();
		Mockito.when(subjectUnderTest.edit(null)).thenReturn(mav);
		
		Assert.assertSame("Should return expected model and view.", mav, subjectUnderTest.create());
		
		Mockito.verify(subjectUnderTest).edit(null);
	}
	
	@Test
	public void testEditNoId() {
		ModelAndView mav = subjectUnderTest.edit(null);
		Assert.assertEquals("Should add new to do to model and view.", new ToDo(), mav.getModel().get("toDo"));
	}
	
	@Test
	public void testEditWithId() {
		Integer id = 1;
		
		ToDo toDo = new ToDo();
		toDo.setId(id);
		Mockito.when(toDoRepository.findOne(id)).thenReturn(toDo);
		
		ModelAndView mav = subjectUnderTest.edit(id);
		Assert.assertSame("Should add the expected to do to model and view.", toDo, mav.getModel().get("toDo"));
	}
	
	@Test
	public void testSaveToDo() {
		String username = "test";
		long now = Calendar.getInstance().getTimeInMillis();
		
		BindingResult validations = Mockito.mock(BindingResult.class);
		
		UserDetails currentUser = Mockito.mock(UserDetails.class);
		Mockito.when(currentUser.getUsername()).thenReturn(username);

		ToDo toDo = new ToDo();
		
		ModelAndView mav = subjectUnderTest.save(toDo, validations, currentUser);
		
		Mockito.verify(toDoRepository).save(toDo);
		
		Assert.assertEquals("Should have set created by.", username, toDo.getCreatedBy());
		Assertions.assertThat(toDo.getCreated().getTimeInMillis()).isBetween(now, now + 1000);
		
		Assert.assertEquals("Should have set updated by.", username, toDo.getUpdatedBy());
		Assertions.assertThat(toDo.getUpdated().getTimeInMillis()).isBetween(now, now + 1000);
		
		Assert.assertEquals("Should have set path to redirect.", "redirect:/",mav.getViewName());
	}
	
	@Test
	public void testDoNotSaveWithValidationErrors() {
		UserDetails currentUser = Mockito.mock(UserDetails.class);
		
		BindingResult validations = Mockito.mock(BindingResult.class);
		Mockito.when(validations.hasErrors()).thenReturn(true);
		
		ToDo toDo = new ToDo();
		
		ModelAndView mav = subjectUnderTest.save(toDo, validations, currentUser);
		
		Assert.assertEquals("Should go back to edit page.", "edit", mav.getViewName());
		
		Mockito.verifyNoMoreInteractions(toDoRepository);
	}
	
	@Test
	public void testListAdminUser() {
		UserDetails user = Mockito.mock(UserDetails.class);
		
		subjectUnderTest = Mockito.spy(subjectUnderTest);
		Mockito.doReturn(true).when(subjectUnderTest).isUserInRole(user, "ROLE_ADMIN");
		
		subjectUnderTest.toDoList(user);
		
		Mockito.verify(toDoRepository).findAll();
	}
	
	@Test
	public void testListNormalUser() {
		String username = "test";
		UserDetails user = Mockito.mock(UserDetails.class);
		Mockito.when(user.getUsername()).thenReturn(username);
		
		subjectUnderTest = Mockito.spy(subjectUnderTest);
		Mockito.doReturn(false).when(subjectUnderTest).isUserInRole(user, "ROLE_ADMIN");
		
		subjectUnderTest.toDoList(user);
		
		Mockito.verify(toDoRepository).findByAssignedToOrCreatedBy(username, username);
	}

}