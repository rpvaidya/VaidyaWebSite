package com.vaidya;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

	
	@RequestMapping("/test")
	
	String test()
	{
	
		try {
			testRedis();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return "TEst Screen";
	}
	@Autowired
	private StringRedisTemplate template;
	
	public void testRedis() throws Exception {
		HashOperations<String, String, String> ops = this.template.opsForHash();
		String hash = "vaidya.salaryAccount";
		String hashKey = "number";
		String HashValue = "004056172"; 
		ops.putIfAbsent(hash, hashKey, HashValue);
		System.out.println("Found key " + hash + ", value=" + ops.get(hash, hashKey));
	}

}
