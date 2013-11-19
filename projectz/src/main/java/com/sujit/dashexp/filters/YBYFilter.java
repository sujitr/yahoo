package com.sujit.dashexp.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sujit.dashexp.CommonUtils;

public class YBYFilter implements Filter {
	private FilterConfig filterConfig = null;
	private String bouncerPageURL = null;
	
	public void destroy() {
		this.filterConfig = null;
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
		boolean isValidUser = false;
		HttpServletRequest siteRequest = (HttpServletRequest)req;
		HttpServletResponse siteResponse = (HttpServletResponse) res;
		Cookie[] cookies = siteRequest.getCookies();
		String userId = new String();
		HttpSession session = siteRequest.getSession(true);
		if(cookies!=null){
			for (Cookie thisCookie : cookies){
		         if(thisCookie.getName().equals("YBY")){
		         	// YBY cookie is present, so a valid user
		         	isValidUser=true;
		         	userId = CommonUtils.getUserIdFromYBYCookie(thisCookie.getValue());
		         }
		     }
		}
		if(isValidUser){
			siteRequest.setAttribute("userid", userId);
			session.setAttribute("userName",userId);
			filterChain.doFilter(siteRequest, siteResponse);
		}else{
			System.out.println("No YBY Cookie detected from :"+siteRequest.getRemoteHost());
			siteResponse.sendRedirect(bouncerPageURL);
		}
		
		// code to bypass filter  - for testing purposes
		/*req.setAttribute("userid", "testuser");
		filterChain.doFilter(req, res); */
	}

	public void init(FilterConfig fc) throws ServletException {
		this.filterConfig = fc;
		if(filterConfig!=null){
			bouncerPageURL = filterConfig.getInitParameter("bouncer-url");
		}
	}

}
