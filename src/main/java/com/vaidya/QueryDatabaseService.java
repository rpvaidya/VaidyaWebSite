package com.vaidya;

import java.util.HashMap;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;



@PropertySource("classpath:application.properties")

@RestController
public class QueryDatabaseService {

	
	@Autowired
	private ApplicationContext context;
	@Autowired
	private StringRedisTemplate template;
	private HashOperations<String, String, String> redisHash;
	private SetOperations<String, String> redisSet;
	@Value("${keyForCategory}")
	private String keyForCategory;

	@RequestMapping("/getTagsForCategory")

	public String[] getTagsForCategory(@RequestParam(value = "category", defaultValue = "Finance") String name) {

		redisSet = template.opsForSet();
		HashSet<String> catSet = (HashSet<String>) redisSet.members(name);
		Object[] objArray = catSet.toArray();
		String[] retArray = new String[objArray.length];
		for (int i = 0; i < objArray.length; i++) {
			retArray[i] = (String) objArray[i];
		}
		return retArray;
	}

	@RequestMapping("/getCategories")

	public String[] getCategories() {

		redisSet = template.opsForSet();
		HashSet<String> catSet = (HashSet<String>) redisSet.members(keyForCategory);
		Object[] objArray = catSet.toArray();
		String[] retArray = new String[objArray.length];
		for (int i = 0; i < objArray.length; i++) {
			retArray[i] = (String) objArray[i];
		}
		return retArray;
	}

	@RequestMapping("/getDetailsForTag")

	public HashMap<String, String> getDetailsForTag(@RequestParam(value = "tag", defaultValue = "") String tag) {
		redisHash = this.template.opsForHash();
		HashMap<String, String> retMap = (HashMap<String, String>) redisHash.entries(tag);
		return retMap;
	}
	
	@RequestMapping(method = RequestMethod.POST, consumes="application/json", produces="application/json", value = "/updateTag")
	@ResponseBody
	public String updateEntity(@RequestBody Accounts acct) {
	    // Do some DB Stuff. Anyway, the control flow does not reach this place.
	    System.out.println(acct.getHash_key());
	    try {
	    	AccountsItemProcessor processor = context.getBean(AccountsItemProcessor.class);
			processor.addModifyAccount(acct);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return "Employee " + acct.getHash_key() + " updated successfully!";
	}
	
}
