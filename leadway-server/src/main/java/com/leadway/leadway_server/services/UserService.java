package com.leadway.leadway_server.services;

import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Optional;

import javax.crypto.*;
import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.leadway.leadway_server.entities.AutoLoginData;
import com.leadway.leadway_server.repositories.AutoLoginDataRepository;
import org.apache.commons.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.leadway.leadway_server.entities.LeadwayUser;
import com.leadway.leadway_server.repositories.UserRepository;

@Component
public class UserService {
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AutoLoginDataRepository autoLoginDataRepository;

	@Autowired
	private MailService mailService;

	@Autowired
	private EncryptionService encryptionService;

	private UserService(){


	}
	
	public ObjectNode createNewUserEntities(ObjectNode signUpForm) throws InvalidKeySpecException, MessagingException, BadPaddingException, IllegalBlockSizeException {
		ObjectNode result = new ObjectMapper().createObjectNode();

		LeadwayUser newUser = new LeadwayUser();
		String userEmail = signUpForm.get("email").asText();
		
		List<LeadwayUser> userWithIdenticalEmail = userRepository.findByEmail(userEmail);
		
		System.out.println(signUpForm);
		if (userWithIdenticalEmail.size() > 0) {
			// if email is already used previously, mark this operation is failed
			result.put("code", 1);
			System.out.println("Email already used");
			return result;
		} else {
			
			String userPassword = signUpForm.get("password").asText();
			String encryptedPassword=encryptionService.PBKDF2Encrypt(userPassword);
			newUser.setEmail(userEmail);
			newUser.setPassword(encryptedPassword);
			userRepository.save(newUser);
			mailService.sendVerificationMailTo(userEmail, encryptionService.AESEncrypt(newUser.getId().toString()));
			result.put("code", 0);
			System.out.println("Email registered");
			return result;	
		}
	}
	
	public ObjectNode loginUser(ObjectNode signInForm, HttpServletResponse httpResponse) throws InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException {
		ObjectNode result = new ObjectMapper().createObjectNode();
		String userEmail = signInForm.get("email").asText();
		String userPassword = signInForm.get("password").asText();
		String encryptedPassword=encryptionService.PBKDF2Encrypt(userPassword);
		List<LeadwayUser> users = userRepository.findByEmailAndPassword(userEmail, encryptedPassword);
		if (users.size() == 0) {
			result.put("code", 1);
			return result;
		} else {
			LeadwayUser foundUser = users.get(0);
			JsonNode userJson = new ObjectMapper().valueToTree(foundUser);
			result.put("code", 0);
			Boolean remeberme=true;
			Long token=new SecureRandom().nextLong();
			String tokenString=foundUser.getId()+":"+token.toString();
			String saltedToken=encryptionService.AESEncrypt(tokenString);
			Optional<AutoLoginData> foundData=autoLoginDataRepository.findById(foundUser.getId());
			AutoLoginData data;
			if(foundData.isPresent()) {
				data=foundData.get();
			} else {
				data=new AutoLoginData();
				data.setUser(foundUser);
			}
			data.setToken(token);
			data.setExpirationTime(System.currentTimeMillis()+(remeberme?7*24*60*60*1000:60*60*1000));
			autoLoginDataRepository.save(data);
			SetLoginToken(httpResponse,saltedToken,remeberme?7*24*60*60:60*60);
			result.set("user", userJson);
			return result;
		}
	}

	public ObjectNode verifyUser(String code) throws BadPaddingException, IllegalBlockSizeException, DecoderException {
		ObjectNode result = new ObjectMapper().createObjectNode();
		String decrypted=encryptionService.AESDecrypt(code);
		Long id;
		try {
			id=Long.parseLong(decrypted);
		}
		catch(Exception ex) {
			result.put("code",1);
			result.put("error","invalid code received! code = "+code);
			return result;
		}
		System.out.println("verify user with id = "+id);
		Optional<LeadwayUser> foundUser=userRepository.findById(id);
		if (!foundUser.isPresent()) {
			result.put("code", 1);
			result.put("error","no user found! code = "+code);
		} else{
			LeadwayUser user=foundUser.get();
			if(user.getType()!=0) {
				result.put("error","user already verified! code = "+code);
				result.put("code", 1);
			} else{
				user.setType(1);
				userRepository.save(user);
				System.out.println("user verified! code = "+code);
				result.put("code", 0);
			}
		}
		return result;
	}

	private void SetLoginToken(HttpServletResponse httpResponse,String token,int expiry)
	{
		Cookie cookie = new Cookie("token", token);
		cookie.setMaxAge(expiry);
		//cookie.setSecure(true);  // requires HTTPS?
		cookie.setPath("/"); // work for whole domain
		httpResponse.addCookie(cookie);
	}
}
