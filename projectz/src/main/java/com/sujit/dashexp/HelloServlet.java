package com.sujit.dashexp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HelloServlet extends HttpServlet {
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setStatus(HttpServletResponse.SC_OK);
		out.println("<h1>Hello World, I am running on Jetty!</h1>");
		out.println("session=" + request.getSession(true).getId());
}
}
