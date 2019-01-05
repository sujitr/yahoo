package com.sujit;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServerMan extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		
		String opt = "{id:\"9\",invdate:\"2007-09-01\",name:\"test3\",note:\"<img src=images/check_mark.gif />\",amount:\"400.00\"}|{id:\"1\",invdate:\"2007-08-10\",name:\"test4\",note:\"<img src=images/check_mark.gif />\",amount:\"800.00\"}|{id:\"2\",invdate:\"2008-07-07\",name:\"test6\",note:\"<img src=images/check_mark.gif />\",amount:\"900.00\"}";
		
		PrintWriter out = response.getWriter();
		response.setStatus(HttpServletResponse.SC_OK);
		System.out.println("Resposne: "+opt);
		out.println(opt);
}
}
