package config.aspect;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class BaseDaoAspect {
    @Before("execution(* service.AirlineService.test(..))")
    public void before(){
        System.out.println("before.......");
    }

    @After("execution(* service.AirlineService.test(..))")
    public void after(){
        System.out.println("after.......");
    }
}
