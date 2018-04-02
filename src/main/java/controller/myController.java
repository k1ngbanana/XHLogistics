package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller("myController")
@RequestMapping("/my")
public class myController {
	@RequestMapping("/index")
	public ModelAndView index() {
		System.out.println("controller");
		ModelAndView mv = new ModelAndView();
		mv.setViewName("index");
		return mv;
	}
}
