/*
 * sf-agile-deploy - Salesforce Agile Deployment tool
 *
 * Copyright © 2017 Igor Androsov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without 
 * restriction, including without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 * SOFTWARE.
 */
package com.force.deploy.start;

// MIT license link https://www.tawesoft.co.uk/kb/article/mit-license-faq
//
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.force.deploy.jgit.CloneRemoteRepositoryWithAuthentication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
//import org.eclipse.jgit.api.errors.GitAPIException;


@RestController
public class StartController {
	private static final String START_DEPLOY_LOG_MSG = "Deployment Tool - Salesforce Agile Accelerator\r\n";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final AtomicInteger counter = new AtomicInteger(0);
	
	private final DeployLog dlog = new DeployLog();

	
	@Value("${java.home}") 
	private String javaHome;

    @RequestMapping("/")
    public String index(HttpServletRequest request) throws MalformedURLException {
    	       	
       	String key = "W-001234";
       	dlog.addLogData(key, "Salesforce Agile Deployment tool message: Nothing to deploy at this time.");
       	
       	String str = myHTML(key,request);
        return str; //"Greetings from Spring Boot!";
    }
    /** Keep this methods for testing
    @RequestMapping("/env")
    public String env(HttpServletRequest request) throws UnknownHostException, MalformedURLException {
    	String s = "====";
        
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n",
                              envName,
                              env.get(envName));
            s += envName+":"+env.get(envName)+" ### ";
        }
            
        DeployEnv e = new DeployEnv();
        s+=" ###### "+e.printEnv();
        
    	return s;
    }
    **/

    public static String getURLBase(HttpServletRequest request) throws MalformedURLException {

        URL requestURL = new URL(request.getRequestURL().toString());
        String port = requestURL.getPort() == -1 ? "" : ":" + requestURL.getPort();
        return requestURL.getProtocol() + "://" + requestURL.getHost() + port;

    }    
    
    
    @RequestMapping("/ne")
    public String one(@RequestParam("story") String storyName) {
    	String s = "";
    	s += dlog.getLogData(storyName); //"W-003243"
    	
    	System.out.println("POLL: "+s);
    	return s;
    }
    
    public static String myHTML(String storyName, HttpServletRequest request) throws MalformedURLException{
		String baseUrl = getURLBase(request);
		String resultUrl = baseUrl+"/ne?story="+storyName;
		
    	String htm = "<html><head><title>sf-agile-deploy</title><script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js\"></script></head><script>";
    	htm += "$(document).ready(function() {";
    	
    	htm += "  setInterval(function(){";
    	//htm += "      $.ajax({ url: \"http://localhost:8080/ne?story="+storyName+"\", ";
    	htm += "      $.ajax({ url: \""+resultUrl+"\", ";
    	htm += "		success: function(data){";  	
    	//htm += "  			console.log(JSON.stringify(data)); $('#txtbox').val(JSON.stringify(data));";
    	htm += "  			$('#txtbox').val($('#txtbox').val() + data);";    	
    	htm += "    	}, ";
    	htm += "		error: function(jqXHR, textStatus, errorThrown) {";
    	htm += "		console.log(errorThrown);	";
    	htm += "      },dataType: \"text\"});";
    	htm += "}, 2000);";
    	
    	htm += "});";
    	htm +="</script><body><H2>Deployment Log Story Name - " + storyName + "</H2><textarea id=\"txtbox\" name=\"txtbox\" rows=\"15\" cols=\"100\">Start deployment log...\r\n</textarea></body></html>";
    	return htm;
    }
 
    
    @RequestMapping("/git")
    public String test(HttpServletRequest request,
    				  @RequestParam("story") String storyName,
    				  @RequestParam("id") String storyId,
    				  @RequestParam("assigned") String assignedId,
    				  @RequestParam("sbn") String sandbox,
    				  @RequestParam("target") String target,
    				  @RequestParam("session") String sessionId,
    				  @RequestParam("api") String api) throws IOException {
    	String msg = "PARAMS: story - "+storyName+" ID: "+storyId+" Session: "+sessionId+" Sandbox: "+sandbox;
    	logger.info(msg);
    	
    	DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
    	
        String str = START_DEPLOY_LOG_MSG;
        dlog.addLogData(storyName, str);
	    new Thread(() -> {

	    	try {
					String res = CloneRemoteRepositoryWithAuthentication.cloneRepo(storyId,storyName,assignedId,sandbox,target,sessionId,api,dlog);
		            result.setResult(ResponseEntity.ok(res));
		        			
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		//str += e.toString();
	    	}
	    }, "DeployThread-" + counter.incrementAndGet()).start();
	    
	    String htm = myHTML(storyName,request);
    	return htm;
    }

    // Pull new changeset and do initial github commit
    @RequestMapping("/pullset")
    public String pullSetRequest(HttpServletRequest request, @RequestParam("story") String storyName,
    				  @RequestParam("id") String storyId,
    				  @RequestParam("assigned") String assignedId,
    				  @RequestParam("sbn") String sandbox,
    				  @RequestParam("target") String target,
    				  @RequestParam("session") String sessionId,
    				  @RequestParam("api") String api) throws IOException {
    	String msg = "PARAMS: story - "+storyName+" ID: "+storyId+" Session: "+sessionId+" Sandbox: "+sandbox+" APIURL: "+api;
    	logger.info(msg);
    	
    	DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
    	
        String str = START_DEPLOY_LOG_MSG;
        dlog.addLogData(storyName, str);
	    new Thread(() -> {

	    	try {
					String res = CloneRemoteRepositoryWithAuthentication.initialCommitChangeSet(storyId,storyName,assignedId,sandbox,target,sessionId,api,dlog);
		            result.setResult(ResponseEntity.ok(res));
		        			
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }, "DeployThread-" + counter.incrementAndGet()).start();
	    
	    String htm = myHTML(storyName,request);
    	return htm;
    }

    // Pull changeset and update github commit changes to remote
    @RequestMapping("/updateset")
    public String updateSetRequest(HttpServletRequest request, @RequestParam("story") String storyName,
    				  @RequestParam("id") String storyId,
    				  @RequestParam("assigned") String assignedId,
    				  @RequestParam("sbn") String sandbox,
    				  @RequestParam("target") String target,
    				  @RequestParam("session") String sessionId,
    				  @RequestParam("api") String api) throws IOException {
    	String msg = "PARAMS: story - "+storyName+" ID: "+storyId+" Session: "+sessionId+" Sandbox: "+sandbox;
    	logger.info(msg);
    	
    	DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
    	
        String str = START_DEPLOY_LOG_MSG;
        dlog.addLogData(storyName, str);
	    new Thread(() -> {

	    	try {
					String res = CloneRemoteRepositoryWithAuthentication.updateCommitChangeSet(storyId,storyName,assignedId,sandbox,target,sessionId,api,dlog);
		            result.setResult(ResponseEntity.ok(res));
		        			
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }, "DeployThread-" + counter.incrementAndGet()).start();
	    
	    String htm = myHTML(storyName,request);
    	return htm;
    }

    // Pull metadata from github cand deploy to target org
    // Update Chatter post after deployment
    @RequestMapping("/deploy")
    public String deployRequest(HttpServletRequest request, @RequestParam("story") String storyName,
    				  @RequestParam("id") String storyId,
    				  @RequestParam("assigned") String assignedId,
    				  @RequestParam("sbn") String sandbox,
    				  @RequestParam("target") String target,
    				  @RequestParam("session") String sessionId,
    				  @RequestParam("api") String api) throws IOException {
    	String msg = "PARAMS: story - "+storyName+" ID: "+storyId+" Session: "+sessionId+" Sandbox: "+sandbox;
    	logger.info(msg);
    	
    	DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
    	
        String str = START_DEPLOY_LOG_MSG;
        dlog.addLogData(storyName, str);
	    new Thread(() -> {

	    	try {
					String res = CloneRemoteRepositoryWithAuthentication.deployChangeSet(storyId,storyName,assignedId,sandbox,target,sessionId,api,dlog);
		            result.setResult(ResponseEntity.ok(res));
		        			
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }, "DeployThread-" + counter.incrementAndGet()).start();
	    
	    String htm = myHTML(storyName,request);
    	return htm;
    }
    
    /// DEPRECATE thjis method
    // public String update(@RequestParam("story") String storyName,
    @RequestMapping("/update")
    public Callable<ResponseEntity<?>> update(@RequestParam("story") String storyName,
    				  @RequestParam("id") String storyId,
    				  @RequestParam("assigned") String assignedId,
    				  @RequestParam("sbn") String sandbox,
    				  @RequestParam("target") String target,
    				  @RequestParam("session") String sessionId,
    				  @RequestParam("api") String api) throws IOException {
    	String msg = "PARAMS: story - "+storyName+" ID: "+storyId+" Session: "+sessionId+" Sandbox: "+sandbox;
    	//System.out.println(msg);
    	logger.info(msg);
    	
        String str = "git OK\r\n";
    	try {
			String res = CloneRemoteRepositoryWithAuthentication.updateRepo(storyId,storyName,assignedId,sandbox,target,sessionId,api);
			str += res + "\r\n";
		} catch (Exception e) {
			e.printStackTrace();
			str += e.toString();
		}
    	final String result = str;
    	//return str;
    	return () -> ResponseEntity.ok(result);
    }
    
}
