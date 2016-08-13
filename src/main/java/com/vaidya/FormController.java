
package com.vaidya;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@Controller
public class FormController {

	@RequestMapping(value = "/editPage", method = RequestMethod.GET)
	public String redirect() {

		return "redirect:CreateAccount.html";
	}
	
	@RequestMapping(value = "/createAccount", method = RequestMethod.GET)
	public String createAccount(@RequestParam Map<String,String> allRequestParams, ModelMap model) {
		System.out.println(allRequestParams.size());
		System.out.println("In the post method");
		return "redirect:Index.html";
	}
	
}
