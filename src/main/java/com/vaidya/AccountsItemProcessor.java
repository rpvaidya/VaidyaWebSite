package com.vaidya;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;;

public class AccountsItemProcessor implements ItemProcessor<Accounts, Accounts> {

	@Autowired
	private StringRedisTemplate template;
	private HashOperations<String, String, String> ops;
	private SetOperations<String, String> redisList;
	
	@Override
	public Accounts process(final Accounts acct) throws Exception {
		ops = this.template.opsForHash();
		redisList = this.template.opsForSet();
		final Accounts transformedAccount = acct;
		// System.out.println(acct.getHash_key());
		if (acct.getHash_key() != "hash_key")
		{
			addCategory(acct.getCategory(), acct.getHash_key());
		}
		
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
		return transformedAccount;
	}

	private boolean addHashKey(String key, Map<String, String> value)

	{
		ops.putAll(key, value);
		return true;

	}

	private Long addCategory(String category, String hashKey) {
		Long retVal = redisList.add(category, hashKey);
		return retVal;
	}
}
