package fr.uha.ensisa.alphapair.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.uha.ensisa.alphapair.exception.APIException;
import fr.uha.ensisa.alphapair.model.Group;
import fr.uha.ensisa.alphapair.model.Promotion;
import fr.uha.ensisa.alphapair.model.Subject;
import fr.uha.ensisa.alphapair.model.User;
import fr.uha.ensisa.alphapair.network.Protocol;
import fr.uha.ensisa.alphapair.repository.GroupRepository;
import fr.uha.ensisa.alphapair.repository.PromotionRepository;
import fr.uha.ensisa.alphapair.repository.SubjectRepository;
import fr.uha.ensisa.alphapair.repository.UserRepository;
import fr.uha.ensisa.alphapair.security.AuthManager;
import fr.uha.ensisa.alphapair.service.logic.GroupLogic;
import fr.uha.ensisa.alphapair.service.logic.PromotionLogic;

@Service
public class PromotionService {
	
	@Autowired
	public PromotionRepository pr;
	
	@Autowired
	public SubjectRepository sr;
	
	@Autowired
	public UserRepository ur;
	
	@Autowired
	public GroupRepository gr;
	
	@Autowired
	public PromotionLogic pl;
	
	@Autowired
	private HttpServletRequest req;
	
	@Autowired
	private HttpServletResponse res;

	public ResponseEntity<Object> addPromotion(String rawPromotion) {
		
		try {
			// checking if an user is logged and is admin
			AuthManager.getLoggedInUserMailFromAccessToken(req.getCookies(), true);
			
			JsonNode json = new ObjectMapper().readTree(rawPromotion);
			
			// creating promotion
			String promotionName = json.get("name").asText();
			String promotionLimitDate = json.get("limitDate").asText();
			boolean promotionIsStudentEditable = json.get("isStudentEditable").asBoolean();
			
			Promotion p = new Promotion(promotionName, promotionLimitDate, promotionIsStudentEditable, new ArrayList<String>());

			//char[] chars = "abcdefghijklmnopqrstuvwxyzABSDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
			char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789".toCharArray();
			int idLength = 6;
			
	    	boolean alreadyExists = true;
	    	Random r = new Random(System.currentTimeMillis());
	    	char[] id = new char[idLength];
	    	
	    	while (alreadyExists) {
	    		for (int i = 0;  i < idLength;  i++) {
	        	    id[i] = chars[r.nextInt(chars.length)];
	        	}
	    		
	    		// checking wether the generated id already exists or not
	    		if (pr.findPromotionById(new String(id)).size() == 0)
	    			alreadyExists = false;
	    	}
	  
	    	p.setId(new String(id)); 

			p = pr.save(p);
			
			List<String> initialChoices = new ArrayList<>();
			
			// creating subjects
			int subjectIndex = 0;
			for (JsonNode subject : json.get("subjects")) {
				String subjectName = subject.get("name").asText();
				String subjectProfessor = subject.get("professor").asText();
				String subjectDescription = subject.get("description").asText();
				
				Subject s = new Subject(String.valueOf(subjectIndex) + "-" + subjectName, subjectProfessor, subjectDescription, p.getId());
				sr.save(s);
				
				// creating a new group for each subject
				Group g = new Group(p.getId(), false, initialChoices);
				gr.save(g);
				
				subjectIndex++;
			}
			
			return new ResponseEntity<Object>(p.getId(), HttpStatus.OK);
		
		} catch (APIException e) {
			return e.getResponseEntity();
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(Protocol.INVALID_ARGUMENT, HttpStatus.BAD_REQUEST);
		}
	}
	
	// creates a mock promotion for testing purposes.
	public ResponseEntity<Object> populatePromotion() {
		try {
			// checking if an user is logged and is admin
			AuthManager.getLoggedInUserMailFromAccessToken(req.getCookies(), true);
			
			// getting the promotion id.
			//char[] chars = "abcdefghijklmnopqrstuvwxyzABSDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
			char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789".toCharArray();
			int idLength = 6;
			
			// generating the mock promotion id
	    	boolean alreadyExists = true;
	    	Random r = new Random(System.currentTimeMillis());
	    	char[] id = new char[idLength];
	    	
	    	while (alreadyExists) {
	    		for (int i = 0;  i < idLength;  i++) {
	        	    id[i] = chars[r.nextInt(chars.length)];
	        	}
	    		
	    		// checking wether the generated id already exists or not
	    		if (pr.findPromotionById(new String(id)).size() == 0)
	    			alreadyExists = false;
	    	}
	    	
	    	String promotionId = new String(id); // we now have generated the mock promotion id
			
			// creating promotion
	    	Promotion p = new Promotion(
	    			"Projets 3A IR 2022-2023",
	    			"2025-01-01",
	    			true,
	    			new ArrayList<String>());
	    	
	    	p.setId(new String(id)); 
			p = pr.save(p);
			
			// creating subjects
			
			int subjectsNumber = 10;
			List<String> subjectIds = new ArrayList<>(); // will store the ids of the generated subjects.
			
			for (int i = 0 ; i < subjectsNumber ; i++) {
				String stringI = String.valueOf(i);
				Subject s = new Subject(stringI + "-Projet " + (i + 1), "Prof no." + stringI, "Desc no." + stringI, promotionId);
				subjectIds.add(sr.save(s).getId()); // saving the subject and saving his returned id. 
			}
			
			// getting the promotion id.
			char[] userChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
			
			// creating groups
			
			int groupsNumber = subjectsNumber;
			int userIndex = 0; 
			
			for (int i = 0 ; i < groupsNumber ; i++) {
				
				// will store the choices of the group
				List<String> groupChoices = new ArrayList<>();

				
				List<Integer> subjectIndexes = new ArrayList<Integer>();
		        for (int j = 0; j < subjectsNumber; j++) subjectIndexes.add(j);
		        Collections.shuffle(subjectIndexes);
		        
		        // adding random subjects to the group choices
		        for (int j = 0 ; j < 3 ; j++) {
		        	groupChoices.add(subjectIds.get(subjectIndexes.get(j)));
		        }
				
				// creating the group
				Group g = new Group(promotionId, false, groupChoices);
				String groupId = gr.save(g).getId();
				
				// creating the group users : we want to add either 1 or 2 users for more variety.
				if (i % 2 == 0) { // add 1 user to the group
					
					char[] userF = new char[idLength];
					char[] userL = new char[idLength];
					for (int j = 0;  j < idLength;  j++) {
						userF[j] = userChars[r.nextInt(userChars.length)];
						userL[j] = userChars[r.nextInt(userChars.length)];
		        	}
					
					String userFString = new String(userF);
					String userLString = new String(userL);
					
				  
					User user = new User(userFString + "." + userLString, "user", userFString, userLString, false, groupId, promotionId);
					ur.save(user);
					userIndex++;
				} else { // add 2 user to the group
					for (int j = 0 ; j < 2 ; j++) {
						
						char[] userF = new char[idLength];
						char[] userL = new char[idLength];
						for (int k = 0;  k < idLength;  k++) {
							userF[k] = userChars[r.nextInt(userChars.length)];
							userL[k] = userChars[r.nextInt(userChars.length)];
			        	}
						
						String userFString = new String(userF);
						String userLString = new String(userL);
						  
						User user = new User(userFString + "." + userLString, "user", userFString, userLString, false, groupId, promotionId);
						ur.save(user);
						userIndex++;
					}
				}
						
				
			}
			
			return new ResponseEntity<Object>(promotionId, HttpStatus.OK);
		
		} catch (APIException e) {
			return e.getResponseEntity();
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(Protocol.INVALID_ARGUMENT, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<Object> getPromotion(String id) {
		try {
			String userMail = AuthManager.getLoggedInUserMailFromAccessToken(req.getCookies(), false);
			User user = ur.findUserByMail(userMail).get(0);
			Promotion p;
			
			if (user.getIsAdmin()) { // if user is an admin
				p = pr.findPromotionById(id).get(0);
				return new ResponseEntity<Object>(p, HttpStatus.OK);
			} else { // if user is not admin, grant access only to his current promotion if defined.
				if (user.getPromotionId() != "") { // student is part of a promotion. give him his infos.
					p = pr.findPromotionById(user.getPromotionId()).get(0);
					return new ResponseEntity<Object>(p, HttpStatus.OK);
				} else { // the student is not part of any promotion.
					return new ResponseEntity<Object>(Protocol.PROMLESS_STUDENT, HttpStatus.BAD_REQUEST);
				}
			}
			
		} catch (APIException e) {
			return e.getResponseEntity();
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(Protocol.INVALID_ARGUMENT, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<Object> getAllPromotions() {
		try {
			// checking if an user is logged and is admin
			AuthManager.getLoggedInUserMailFromAccessToken(req.getCookies(), true);
			
			List<Promotion> promotions = pr.findAll();
			return new ResponseEntity<Object>(promotions, HttpStatus.OK);
			
		} catch (APIException e) {
			return e.getResponseEntity();
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(Protocol.INVALID_ARGUMENT, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<Object> updatePromotionField(String id, String rawPromotionUpdate) {
		try {
			// checking if an user is logged and is admin
			AuthManager.getLoggedInUserMailFromAccessToken(req.getCookies(), true);
			
			JsonNode json = new ObjectMapper().readTree(rawPromotionUpdate);
			
			String promotionField = json.fieldNames().next();
			String promotionValue = json.get(promotionField).asText();
			
			if (promotionField == "name")
				pr.updatePromotionName(id, promotionValue);
			else if (promotionField == "limitDate")
				pr.updatePromotionLimitDate(id, promotionValue);
			else if (promotionField == "isStudentEditable") {
				pr.updatePromotionIsStudentEditable(id, Boolean.parseBoolean(promotionValue));
			} else
				throw new Exception("Unknown db field.");
			
			return new ResponseEntity<Object>(null, HttpStatus.OK);
			
		} catch (APIException e) {
			return e.getResponseEntity();
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(Protocol.INVALID_ARGUMENT, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<Object> deletePromotion(String id) {
		try {
			// checking if an user is logged and is admin
			AuthManager.getLoggedInUserMailFromAccessToken(req.getCookies(), true);
			
			// deleting promotion
			pr.deleteById(id);
			
			// deleting promotion-related subjects
			sr.deletePromotionSubjects(id);
			
			// deleting promotion-related groups
			gr.deletePromotionGroups(id);
			
			// ejecting users who were part of the promotion
			ur.ejectPromotionUsers(id);
			
			return new ResponseEntity<Object>(null, HttpStatus.OK);
			
		} catch (APIException e) {
			return e.getResponseEntity();
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(Protocol.INVALID_ARGUMENT, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<Object> generatePromotionAssignment(String id) {
		try {
			String userMail = AuthManager.getLoggedInUserMailFromAccessToken(req.getCookies(), false);
			User user = ur.findUserByMail(userMail).get(0);
			
			Promotion p;
			
			if (user.getIsAdmin()) { // if user is an admin
				
				pr.getById(id); // we just do this to check that the promotion does exist.
				return pl.generatePromotionAssignment(id);
				
			} else { // if user is not admin, grant access only to his current promotion if defined.
				
				if (user.getPromotionId() != "") { // student is part of a promotion.
					 p = pr.findPromotionById(user.getPromotionId()).get(0);
					 System.out.println("ISSTUDENTEDITABLE : " + p.getIsStudentEditable());
					
					if (p.getIsStudentEditable() /* &&  date limite ok && cooldown OK*/) {
						return pl.generatePromotionAssignment(user.getPromotionId());
						
					} else {
						return new ResponseEntity<Object>(Protocol.SERVER_LOGIC_ERROR, HttpStatus.FORBIDDEN);
					}

				} else { // the student is not part of any promotion.
					return new ResponseEntity<Object>(Protocol.PROMLESS_STUDENT, HttpStatus.BAD_REQUEST);
				}
			}
			
		} catch (APIException e) {
			return e.getResponseEntity();
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(Protocol.INVALID_ARGUMENT, HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<Object> getPromotionStudents(String id) {
		try {
			String userMail = AuthManager.getLoggedInUserMailFromAccessToken(req.getCookies(), false);
			User user = ur.findUserByMail(userMail).get(0);
			Promotion p;
			
			if (user.getIsAdmin()) { // if user is an admin
				pr.getById(id); // we just do this to check that the promotion does exist.
				return pl.getPromotionStudents(id);

			} else { // if user is not admin, grant access only to his current promotion if defined.
				if (user.getPromotionId() != "") { // student is part of a promotion. give him his infos.
					return pl.getPromotionStudents(user.getPromotionId());

				} else { // the student is not part of any promotion.
					return new ResponseEntity<Object>(Protocol.PROMLESS_STUDENT, HttpStatus.BAD_REQUEST);
				}
			}
			
		} catch (APIException e) {
			return e.getResponseEntity();
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(Protocol.INVALID_ARGUMENT, HttpStatus.BAD_REQUEST);
		}
	}

	
	
	
}
