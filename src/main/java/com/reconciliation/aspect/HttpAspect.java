package com.reconciliation.aspect;

import com.reconciliation.util.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 记录每次的http请求
 *   每次请求http之前 记录
 * @author Administrator
 *
 */
@Aspect
@Order(-99) // 控制多个Aspect的执行顺序，越小越先执行
@Component
public class HttpAspect {

	private final static Logger logger = LoggerFactory.getLogger(HttpAspect.class);


	
	/**
	 * @Pointcut 可以减少重复代码,直接引用@Pointcut这个注解的方法名
	 */
	//@Pointcut("execution(public * com.weixin.controller.TestController.findUserAll(..))")
	@Pointcut("execution(public * com.reconciliation.controller.FinanceController.*(..))")
	public void log() {
	}
	//@Pointcut(value = "execution(public * com.weixin.controller.TestController.*(..))")
	@Pointcut(value = "execution(public *  com.reconciliation.controller.FinanceController.*(..))")
	public void logAll() {
		
	}
	
	@Before("log()")
  	public void logBefore(JoinPoint joinPoint) throws IOException {

		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		//url
		logger.info("url={}",request.getRequestURL());
		//method
		logger.info("method={}",request.getMethod());
		//ip
		logger.info("ip={}",request.getRemoteAddr());
		//类方法
		logger.info("class_method={}",joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
		//参数
		logger.info("args={}",joinPoint.getArgs());
		logger.info(">>>>>>>>>>>>>>");

	}
	
	
	@After("logAll()")
  	public void logAfter() {
		logger.info("<<<<<<<<<<<<<<");
	}

	@Around("logAll()")
	public void around(ProceedingJoinPoint pjp) throws Throwable{
		logger.info("已经记录下操作日志@Around 方法执行前");
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();

		String paramStr = null;
		Enumeration enu=request.getParameterNames();
		while(enu.hasMoreElements()) {
			String paraName = (String) enu.nextElement();
			//System.out.println(paraName + ": " + request.getParameter(paraName));
			paramStr = request.getParameter(paraName);
		}
		//http请求参数中有+号就会变成空格 要替换
		paramStr = paramStr.replace(" ","+");
		//参数解密
		String decryptStr = SecurityUtils.decrypt(paramStr);
		if(decryptStr==null){
			return;
		}

		//json字符串转换map对象
		/*Map map = JsonUtil.jsonToMap(decryptStr);

		//检测数据的有效性
		int count = parkingMapper.checkDataIsValid(map);
		if(count==0){
			return;
		}*/

		pjp.proceed();



		logger.info("已经记录下操作日志@Around 方法执行后");
	}

}
