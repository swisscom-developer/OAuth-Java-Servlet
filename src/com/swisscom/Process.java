package com.swisscom;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class Process
 */
@SuppressWarnings("deprecation")
@WebServlet("/Process")
public class Process extends HttpServlet {
	private static final long serialVersionUID = 1L;


	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Process() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		/*
		 * First call:
		 * Get Authorization Code 
		 * ... redirects to the calling servlet
		 * 
		 * URL params:
		 * 
		 * response_type = code  
		 * redirect_uri = Note that the redirect_uri given as parameter must match the Callback URL configured 
		 * in the Developer Portal. For example, if you set Callback URL to https://www.example.com/, you can redirect 
		 * the user to https://www.example.com/a_page.html or https://www.example.com/?auth=true 
		 * (but not https://www.example.com/auth).
		 * client_id = Created key on the Swisscom Developer Portal
		 * ---
		 * Header:
		 * 
		 * Accept: text/html
		 * ---
		 */
		String code = request.getParameter("code");
		String error = request.getParameter("errorCode");
		Logger logger = Logger.getLogger("Process");
		if (code == null && error == null ) {
			logger.info("Redirect");		
			response.addHeader("Accept", "text/html");
			String consentUrl = "https://consent.swisscom.com/c/oauth2/auth?"
                    + "response_type=code"
                    + "&redirect_uri="
                    + "%REDIRECT_URI(url encoded)%"
                    + "&client_id=%YOUR_CLIENT_ID_GOES_HERE%";
			logger.info("ConsentUrl: " + consentUrl);
			response.sendRedirect(consentUrl);
			
		/*
		 * If the code is set wrong
		 */
		}else if(error != null){
			logger.info("Seems like we've got an error. ErrorCode: "
						+ request.getParameter("errorCode"));
		
			
		/*
		 * Second Call:
		 * Get Access Token
		 * ... HTTP Request to get oauth access token
		 * 
		 * URL params:
		 *  
		 * grant_type = authorization_code
		 * code = code of the first calls response
		 * redirect_uri = as in the first call
		 * ---
		 * Header:
		 * 
		 * Accept: application/json;charset=utf-8
		 * Authorization: Basic {clientId:ClientSecret base64-encoded}
		 * 
		 */
		} else {
			logger.info("ProcessCode");
			logger.info("Got code:" + code + " from request");
			String consentUrl = "https://consent.swisscom.com/o/oauth2/token?"
                    + "grant_type=authorization_code"
                    + "&code="
                    + "%YOUR_CODE_GOES_HERE%"
                    + "&redirect_uri=%REDIRECT_URI(url encoded)%";

			logger.info("ConsentUrl: " + consentUrl);
			
			HttpClient client = new DefaultHttpClient();
			HttpGet newRequest  = new HttpGet(consentUrl);
			newRequest.setHeader("Accept", "application/json;charset=utf-8");
			newRequest.setHeader("Authorization",
					"Basic %YOUR_CLIENT_ID%:%YOUR_CLIENT_ID_SECRET%(As BASE64)");
			HttpResponse httpresponse = null;
			httpresponse = client.execute(newRequest);
	       
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					httpresponse.getEntity().getContent()));	
			logger.info(httpresponse.getStatusLine().getStatusCode());
			String line = "";
			String accesstoken = "";
			for(int i=0;i<6;i++){
				line=rd.readLine();
				logger.info(line);
				if(i==1){
					//Get the access token
					String[] index = line.split(":");
					accesstoken = index[1];
					accesstoken = accesstoken.substring(2,accesstoken.length()-2);
//					logger.info(accestoken);
				}
			}
			
			
			//Call the Login API - to show how to use the Acces Token
			if(httpresponse.getStatusLine().getStatusCode()==200){
				String loginURL = "https://api.swisscom.com/v1/users/~/login";
				HttpClient defaultClient = new DefaultHttpClient();
				HttpGet loginRequest  = new HttpGet(loginURL);
				loginRequest.setHeader("Authorization","Bearer "+ accesstoken);
				HttpResponse loginhttpresponse = client.execute(loginRequest);
		       
				
				// Get the response
				BufferedReader loginrd = new BufferedReader(new InputStreamReader(
						loginhttpresponse.getEntity().getContent()));	
				logger.info(loginhttpresponse.getStatusLine().getStatusCode());
				String loginline = "";

				while ((loginline = loginrd.readLine()) != null) {
					logger.info(loginline);
				}
			}
		
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// has not been used
	}

}
