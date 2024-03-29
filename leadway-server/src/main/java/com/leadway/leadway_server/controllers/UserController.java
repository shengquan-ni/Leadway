package com.leadway.leadway_server.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.leadway.leadway_server.services.UserService;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
public class UserController {

	@Autowired
	private UserService service;
	
	@RequestMapping(method=RequestMethod.POST, value="/api/register")
	public ObjectNode register(@RequestBody String request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) 
			throws IOException, InvalidKeySpecException, MessagingException, NoSuchPaddingException, InvalidKeyException, 
			NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		
		ObjectNode signUpForm = (ObjectNode) new ObjectMapper().readTree(request);
		return service.createNewUserEntities(signUpForm);
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/api/login")
	public ObjectNode signIn(@RequestBody String request,HttpServletRequest httpRequest, HttpServletResponse httpResponse) 
			throws IOException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException, 
			InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
		
		ObjectNode signInForm = (ObjectNode) new ObjectMapper().readTree(request);
		ObjectNode result = service.loginUser(signInForm, httpResponse);
		return result;
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/api/logout")
	public ObjectNode signOut(@RequestBody String request,HttpServletRequest httpRequest, HttpServletResponse httpResponse) 
			throws IOException, BadPaddingException, IllegalBlockSizeException, DecoderException {
		ObjectNode logoutInfo = (ObjectNode) new ObjectMapper().readTree(request);
		ObjectNode result = service.logoutUser(logoutInfo, httpResponse);
		return result;
	}

	@RequestMapping(method=RequestMethod.GET, value="/api/verify")
	public ObjectNode verifyUser(@RequestParam("code") String code) throws NoSuchPaddingException, BadPaddingException, 
			InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, DecoderException, InvalidAlgorithmParameterException, 
			UnsupportedEncodingException {
		System.out.println("in verify get");
		return service.verifyUser(code);
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/api/verify")
	public ObjectNode verifyUser2(@RequestParam("code") String code) throws NoSuchPaddingException, BadPaddingException, 
			InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, DecoderException, InvalidAlgorithmParameterException, 
			UnsupportedEncodingException {
		System.out.println("in verify post");
		return service.verifyUser(code);
	}
}
