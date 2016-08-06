package com.vaidya;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;;
@PropertySource("classpath:application.properties")
public class AccountsItemProcessor implements ItemProcessor<Accounts, Accounts> {

	@Autowired
	private StringRedisTemplate template;
	private HashOperations<String, String, String> ops;
	private SetOperations<String, String> redisSet;
	
	@Value("${keyForCategory}")
	private String keyForCategory;
	
	
	@Override
	public Accounts process(final Accounts acct) throws Exception {
		ops = this.template.opsForHash();
		redisSet = this.template.opsForSet();
		final Accounts transformedAccount = acct;
		// System.out.println(acct.getHash_key());
		
		if (acct.getHash_key() != "hash_key" && acct.getCategory().length() !=0)
		{
			System.out.print(".......Adding Category.........." +acct.getCategory() + "\n");
			addCategory(acct.getCategory());
			System.out.print(".......Adding Tag...." + acct.getHash_key()+ "\n");
			addTagForCategory(acct.getCategory(), acct.getHash_key());
		}

		
		System.out.print(".......Begin : Adding Details for Tag...." + acct.getHash_key() + "\n");

		
		HashMap<String, String> keyMap = new HashMap<>();
		keyMap.put("belongs" , acct.getBelongs()) ;
		keyMap.put("number" , acct.getNumber()) ;
		keyMap.put("category" , acct.getCategory()) ; 
		keyMap.put("description" , acct.getDescription()) ; 
		keyMap.put("valid_from" , acct.getValid_from()); 
		keyMap.put("valid_to" , acct.getValid_to() );
		keyMap.put("address" , acct.getAddress() );
		keyMap.put("name" , acct.getName() );
		keyMap.put("web_address",acct.getWeb_address() );
		keyMap.put("uid" , acct.getUid() );
		keyMap.put("pwd" , acct.getPwd());
		this.addHashKey(acct.getHash_key(), keyMap);
		
		System.out.print(".......End : Adding Details for Tag...." + acct.getHash_key() + "\n");

		
		return transformedAccount;
	}

	private boolean addHashKey(String key, Map<String, String> value)

	{
		ops.putAll(key, value);
		return true;

	}
	
	private Long addCategory(String category) {
		Long retVal = redisSet.add(keyForCategory,category);
		return retVal;
	}
	

	private Long addTagForCategory(String category, String hashKey) {
		Long retVal = redisSet.add(category, hashKey);
		return retVal;
	}
}
