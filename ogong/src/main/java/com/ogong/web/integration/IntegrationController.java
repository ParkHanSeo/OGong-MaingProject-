package com.ogong.web.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ogong.common.Page;
import com.ogong.common.Search;
import com.ogong.service.admin.AdminService;
import com.ogong.service.banana.BananaService;
import com.ogong.service.domain.Answer;
import com.ogong.service.domain.Banana;
import com.ogong.service.domain.Message;
import com.ogong.service.domain.Notice;
import com.ogong.service.domain.User;
import com.ogong.service.integration.IntegrationService;

@Configuration 
@EnableScheduling
@Controller
@RequestMapping("/integration/*")
public class IntegrationController {
	
	@Autowired
	private IntegrationService integrationService;
	@Autowired
	private AdminService adminService;
	@Autowired
	private BananaService bananaService;	
	
	public IntegrationController() {
		System.out.println(this.getClass());
	}
	
	/*@GetMapping("addSendMessage")
	public String addSendMessage(Model model) throws Exception{
		
		System.out.println("/integration/addSendMessage : GET");
		
		User sender = new User();
		Message message = new Message();
		
		sender.setEmail("user06");
		message.setSender(sender);
		
		model.addAttribute(message);
		
		System.out.println(message+"여기는 message");
		
		return "/integrationView/addSendMessage";
		
	}*/
	
	//쪽지 전송을 위한 메소드
	@PostMapping("addSendMessage")
	public String addSendMessage( @ModelAttribute("message") Message message,
								  HttpSession session, 
								  Notice notice) throws Exception{
		
		System.out.println("message 시작");
		
		User user = (User)session.getAttribute("user"); 
		message.setSender(user);
		
		System.out.println("message 확인 ::: " +message);
		System.out.println("session user 확인 :::"+session.getAttribute("user"));
		
		// 알림 처리를 위해 알림 insert부터 해 볼까요
		notice.setNoticeUser(message.getReceiver());
		notice.setSender(user);
		notice.setNoticeCategory("7");
		notice.setNoticeCondition("2");
		
		
		
		integrationService.addNotice(notice);
		
		
		// 이제 쪽지 전송처리를 합시다.
		System.out.println("message : : : "+message);
		integrationService.addSendMessage(message);
		integrationService.addSendMessage2(message);
		System.out.println(message.getSender().getEmail());
		
		return "redirect:/integration/listSendMessage?sender.email="+message.getSender().getEmail();
	}
	
	//받은 쪽지 목록
	@RequestMapping(value="listSendMessage")
	public String listSendMessage(@ModelAttribute("search") Search search, HttpSession session,
									@ModelAttribute("message") Message message, Model model)throws Exception {

		int pageSize = 30;
		int pageUnit = 20;
	
		System.out.println("/integration/listSendMessage : GET");
		
		User email = (User)session.getAttribute("user");
		message.setSender(email);
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("search", search);
		map.put("message", message);
		
		System.out.println("message는 이거 :::::"+message);
		
		Map<String,Object> result = integrationService.getlistSendMessage(map);
		List<Object> list = (List<Object>)result.get("list");
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)result.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println("여기는 resultPage " + resultPage);
				
		model.addAttribute("list", result.get("list"));
		model.addAttribute("search", search);
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("message", message);
		
		return "/integrationView/listSendMessage";
	}
	
	//보낸 쪽지 목록
	@RequestMapping(value="listReceiveMessage")
	public String listReceiveMessage(@ModelAttribute("search") Search search, Model model, HttpSession session, Message message  )throws Exception {
		
		int pageSize = 30;
		int pageUnit = 20;
		
		System.out.println("/integration/listReceiveMessage : GET");
		
		User email = (User)session.getAttribute("user");
		message.setReceiver(email);
		message.setSender(email);
		
		
		//쪽지 전송으로 session END....		
		
		System.out.println("/integration/listReceiveMessage : GET");
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("search", search);
		map.put("message", message);
		
		Map<String,Object> result = integrationService.getlistReceiveMessage(map);
		List<Object> list = (List<Object>)result.get("list");
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)result.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println("여기는 resultPage " + resultPage);
				
		model.addAttribute("list", result.get("list"));
		model.addAttribute("search", search);
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("message", message);
		
		return "/integrationView/listReceiveMessage";
	}
	
	//쪽지 선택삭제
	@PostMapping("deleteTest")
	public void deleteTest(@RequestParam(value = "messageNo[]") List<String> messageArr, 
						  Message message) throws Exception{
			
		System.out.println("테스트 삭제 실행");
			
			
			User user = new User();
			
			message.setReceiver(user);
			
			int result = 0;
			int messageNo = 0;
			
			if(user != null) {
				message.setReceiver(user);
				
				  for(String i : messageArr) {   
					  messageNo = Integer.parseInt(i);
					   message.setMessageNo(messageNo);
					   integrationService.deleteMessage(message.getMessageNo());
				  }
				  
				  result = 1;
					
			}
		
		
		
	}

	
	//메인페이지의 필요한 모든 것
	@GetMapping("mainPage")
	public String mainPage(Model model, Answer answer, User user, HttpSession session) throws Exception{
		
		System.out.println("mainPage 메소드가 실행되는지 확인합시다."); 
		
		User email = (User)session.getAttribute("user");
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		
		List<User> banana = integrationService.listBananaRanking(map);
		
		
		List<Answer> choose = integrationService.listChooseCountRanking(map);
		
		
		map.put("banana", banana);
		map.put("choose", choose);
			
		System.out.println("확인합시다. ::: "+choose.get(0));
		System.out.println("확인합시다. ::: "+choose.get(1));
		System.out.println("확인합시다. ::: "+choose.get(2));
		
		
		model.addAttribute("user", session.getAttribute("user"));
		model.addAttribute("list2", map.get("banana"));
		model.addAttribute("list", map.get("choose"));
		
		return "/index";
	}
	
	//바나나 랭킹 1위~3위 일주일에 한 번 바나나 지급
	//@Scheduled(fixedRate = 20000) 
	public void bananaAdd() throws Exception{
		
		System.out.println("바나나 수 랭킹 일정시간 포인트 지급을 확인합시다.");
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<User> bananaRank = integrationService.listBananaRanking(map);
		
		User user = new User();
		Banana banana1 = new Banana();
		Banana banana2 = new Banana();
		Banana banana3 = new Banana();
		
		//바나나 랭킹 1위
		banana1.setBananaEmail(bananaRank.get(0));
		banana1.setBananaAmount(100);
		banana1.setBananaHistory("바나나 랭킹 1위로 인한 포인트 지급");
		banana1.setBananaCategory("1");
		bananaService.addBanana(banana1);
		user.setEmail(bananaRank.get(0).getEmail());
		user.setBananaCount(100);
		bananaService.updateAcquireBanana(user);
		//바나나 랭킹 2위
		banana2.setBananaEmail(bananaRank.get(1));
		banana2.setBananaAmount(50);
		banana2.setBananaHistory("바나나 랭킹 2위로 인한 포인트 지급");
		banana2.setBananaCategory("1");
		bananaService.addBanana(banana2);
		user.setEmail(bananaRank.get(1).getEmail());
		user.setBananaCount(50);
		bananaService.updateAcquireBanana(user);
		//바나나 랭킹 3위
		banana3.setBananaEmail(bananaRank.get(0));
		banana3.setBananaAmount(30);
		banana3.setBananaHistory("바나나 랭킹 3위로 인한 포인트 지급");
		banana3.setBananaCategory("1");
		bananaService.addBanana(banana3);
		user.setEmail(bananaRank.get(2).getEmail());
		user.setBananaCount(30);
		bananaService.updateAcquireBanana(user);
	
	}
	
	//채택수 랭킹 1위~3위 일주일에 한 번 바나나 지급	
	//@Scheduled(fixedRate = 20000)
	public void chooseAdd() throws Exception{
		
		System.out.println("바나나 수 랭킹 일정시간 포인트 지급을 확인합시다.");
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<Answer> chooseRank = integrationService.listChooseCountRanking(map);
		
		User user = new User();
		Banana choose1 = new Banana();
		Banana choose2 = new Banana();
		Banana choose3 = new Banana();
		
		//채택랭킹 1위
		choose1.setBananaEmail(chooseRank.get(0).getAnswerWriter());
		choose1.setBananaAmount(100);
		choose1.setBananaHistory("채택수 랭킹 1위로 인한 포인트 지급");
		choose1.setBananaCategory("1");
		bananaService.addBanana(choose1);
		user.setEmail(chooseRank.get(0).getAnswerWriter().getEmail());
		user.setBananaCount(100);
		bananaService.updateAcquireBanana(user);
		//채택랭킹 2위
		choose2.setBananaEmail(chooseRank.get(1).getAnswerWriter());
		choose2.setBananaAmount(50);
		choose2.setBananaHistory("채택수 랭킹 2위로 인한 포인트 지급");
		choose2.setBananaCategory("1");
		bananaService.addBanana(choose2);
		user.setEmail(chooseRank.get(1).getAnswerWriter().getEmail());
		user.setBananaCount(50);
		bananaService.updateAcquireBanana(user);
		//채택랭킹 3위
		choose2.setBananaEmail(chooseRank.get(2).getAnswerWriter());
		choose2.setBananaAmount(30);
		choose2.setBananaHistory("채택수 랭킹 3위로 인한 포인트 지급");
		choose2.setBananaCategory("1");
		bananaService.addBanana(choose3);
		user.setEmail(chooseRank.get(2).getAnswerWriter().getEmail());
		user.setBananaCount(30);
		bananaService.updateAcquireBanana(user);
		
		
		

	}
	
	
}












