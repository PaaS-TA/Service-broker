package sampleApp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.cloud.app.ApplicationInstanceInfo;

import sampleApp.common.CloudFoundryConnector;

@Controller
public class MainController {
 
	@RequestMapping("/main")
    public String main(Model model) {
		
		CloudFoundryConnector con = new CloudFoundryConnector();
    	ApplicationInstanceInfo instance_info = con.getApplicationInstanceInfo();
    	
    	System.out.println("=== ApplicationInstanceInfo ==========");
    	System.out.println("-- getAppId() : " + instance_info.getAppId());
    	System.out.println("-- getInstanceId() : " + instance_info.getInstanceId());
    	System.out.println("-- getProperties() : "+ instance_info.getProperties().toString());
    	
    	for (Map<String, Object> item : (List<Map<String, Object>>)con.getServicesData()) {  		
    		Map<String, Object> service_info = (Map<String, Object>)item.get("credentials"); 		
    		String naverMapUrl;
    		if("publicPerformance".equals(item.get("name"))){
    			model.addAttribute("publicPerformance_Url", service_info.get("url"));
    			model.addAttribute("publicPerformance_Key", service_info.get("serviceKey"));    			
    		}
    		if("incheonCulture".equals(item.get("name"))){
    			model.addAttribute("incheonCulture_Url", service_info.get("url"));
    			model.addAttribute("incheonCulture_Key", service_info.get("serviceKey"));    			
    		}
    		if("daejeonFestival".equals(item.get("name"))){
    			model.addAttribute("daejeonFestival_Url", service_info.get("url"));
    			model.addAttribute("daejeonFestival_Key", service_info.get("serviceKey"));    			
    		}
    		if("jeonnamPerformanceList".equals(item.get("name"))){
    			model.addAttribute("jeonnamPerformanceList_Url", service_info.get("url"));
    			model.addAttribute("jeonnamPerformanceList_Key", service_info.get("serviceKey"));    			
    		}
    		if("naverMap".equals(item.get("name"))){
    			naverMapUrl = service_info.get("url")+"?ver=2.0&key="+service_info.get("serviceKey");
    			model.addAttribute("naverMap_Key", service_info.get("serviceKey"));
    			
    	    	String script1 ="<script type=";
    	    	String script2 ="text/javascript";
    	    	String script3 = " src=";
    	    	String script4 = ">";
    	    	String script5 = "/script>";
    	    	model.addAttribute("script1",script1);
    	    	model.addAttribute("script2",script2);
    	    	model.addAttribute("script3",script3);
    	    	model.addAttribute("script4",script4);
    	    	model.addAttribute("script5",script5);
    			model.addAttribute("naverMapUrl",naverMapUrl);
    		}
    		if("naverAddressToGPS".equals(item.get("name"))){
    			model.addAttribute("naverAddressToGPS_Url", service_info.get("url"));
    			model.addAttribute("naverAddressToGPS_Key", service_info.get("serviceKey"));    			
    		}
    		if("naverSearch".equals(item.get("name"))){
    			model.addAttribute("naverSearch_Url", service_info.get("url"));
    			model.addAttribute("naverSearch_Key", service_info.get("serviceKey"));    			
    		}	
    	}
        return "main";
    }
}
