package com.vaidya;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

public class AccountsRedisItemWriter<T> implements ItemWriter<T> {

	@Override
	public void write(List<? extends T> arg0) throws Exception {
		// TODO Auto-generated method stub

		for (int i = 0; i < arg0.size(); i++) {
			System.out.print("...........Hash Key : " + ((Accounts) arg0.get(i)).getHash_key() + "\n");
		}

	}

}
