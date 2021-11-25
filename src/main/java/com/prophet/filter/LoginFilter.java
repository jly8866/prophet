package com.prophet.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.core.annotation.Order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//Order表示顺序，数字越小越优先处理
@Order(1)		
@WebFilter(filterName = "loginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter{
	
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		/**
		 * 这里可以强制转换是因为HttpServletRequest和ServletRequest都是接口,前者继承自后者。
		 * ServletRequest request并不是ServletRequest的实例，强制转换安全。
		 */
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		//判断URL白名单
		String requestURI = request.getRequestURI();
		if (
				!requestURI.equals("/login.json") &&
				!requestURI.equals("/logout.json")
			) {
			HttpSession session = request.getSession();
			
			if (session == null || session.getAttribute("loginedUser") == null) {
				//如果没有登录，则直接返回，不到后端执行请求
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("status", 2);
				m.put("message", "unlogin");
				m.put("data", null);
				ObjectMapper mapper = new ObjectMapper();
				String jsonResult = mapper.writeValueAsString(m);
				
				//将json作为http响应输出给前端
				ServletOutputStream out = servletResponse.getOutputStream();
				out.print(jsonResult);
				out.close();
			} else {
				//如果已登录，则到后端执行请求
				filterChain.doFilter(servletRequest, servletResponse);
			}
		} else {
			filterChain.doFilter(servletRequest, servletResponse);
		}
		
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}

	@Override
	public void destroy() {
		
	}
}
