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
    			//바인드할 때 서비스키를 입력받지 못하는 경우가 있기 때문에(CF버전 관련문제 때문에) 서비스 키를 직접 입력
    			String serviceKey = "2qdkn0RgbXqlX%2BsIQClWOQ0cJD0a1U%2B%2BleTZxQHPvKJU3ip773a6%2BtQ4qVUemaNno6NPX7jrdbD4L45xhHrNzQ%3D%3D";		
    			//바인드할 때 서비스 키를 입력받도록 하는 경우
    			//String serviceKey = (String)service_info.get("serviceKey");
    			
    			model.addAttribute("publicPerformance_Url", service_info.get("url"));
    			model.addAttribute("publicPerformance_Key", serviceKey);    			
    		}
    		if("incheonCulture".equals(item.get("name"))){
    			//바인드할 때 서비스키를 입력받지 못하는 경우가 있기 때문에(CF버전 관련문제 때문에) 서비스 키를 직접 입력
    			String serviceKey = "3JBTmj7URcdqnCZbEP1fGgStuDyax8";
    			//바인드할 때 서비스 키를 입력받도록 하는 경우
    			//String serviceKey = (String)service_info.get("serviceKey");
    			
    			model.addAttribute("incheonCulture_Url", service_info.get("url"));
    			model.addAttribute("incheonCulture_Key", serviceKey);    			
    		}
    		if("daejeonFestival".equals(item.get("name"))){
    			//바인드할 때 서비스키를 입력받지 못하는 경우가 있기 때문에(CF버전 관련문제 때문에) 서비스 키를 직접 입력
    			String serviceKey = "2qdkn0RgbXqlX%2BsIQClWOQ0cJD0a1U%2B%2BleTZxQHPvKJU3ip773a6%2BtQ4qVUemaNno6NPX7jrdbD4L45xhHrNzQ%3D%3D";
    			//바인드할 때 서비스 키를 입력받도록 하는 경우
    			//String serviceKey = (String)service_info.get("serviceKey");
    			model.addAttribute("daejeonFestival_Url", service_info.get("url"));
    			model.addAttribute("daejeonFestival_Key", serviceKey);    			
    		}
    		if("jeonnamPerformanceList".equals(item.get("name"))){
    			//바인드할 때 서비스키를 입력받지 못하는 경우가 있기 때문에(CF버전 관련문제 때문에) 서비스 키를 직접 입력
    			String serviceKey = "2qdkn0RgbXqlX%2BsIQClWOQ0cJD0a1U%2B%2BleTZxQHPvKJU3ip773a6%2BtQ4qVUemaNno6NPX7jrdbD4L45xhHrNzQ%3D%3D";
    			//바인드할 때 서비스 키를 입력받도록 하는 경우
    			//String serviceKey = (String)service_info.get("serviceKey");
    			
    			model.addAttribute("jeonnamPerformanceList_Url", service_info.get("url"));
    			model.addAttribute("jeonnamPerformanceList_Key", serviceKey);    			
    		}
    		if("naverMap".equals(item.get("name"))){
    			//바인드할 때 서비스키를 입력받지 못하는 경우가 있기 때문에(CF버전 관련문제 때문에) 서비스 키를 직접 입력
    			String serviceKey = "a83e142ec626b38627d97b89647ce897";
    			//바인드할 때 서비스 키를 입력받도록 하는 경우
    			//String serviceKey = (String)service_info.get("serviceKey");
    			
    			naverMapUrl = service_info.get("url")+
    								"?ver=2.0&key="+serviceKey;
    			
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
    			model.addAttribute("naverMap_Url",naverMapUrl);
    		}
    		if("naverAddressToGPS".equals(item.get("name"))){
    			//바인드할 때 서비스키를 입력받지 못하는 경우가 있기 때문에(CF버전 관련문제 때문에) 서비스 키를 직접 입력
    			String serviceKey = "a83e142ec626b38627d97b89647ce897";
    			//바인드할 때 서비스 키를 입력받도록 하는 경우
    			//String serviceKey = (String)service_info.get("serviceKey");
    			
    			model.addAttribute("naverAddressToGPS_Url", service_info.get("url"));
    			model.addAttribute("naverAddressToGPS_Key", serviceKey);    			
    		}
    		if("naverSearch".equals(item.get("name"))){
    			//바인드할 때 서비스키를 입력받지 못하는 경우가 있기 때문에(CF버전 관련문제 때문에) 서비스 키를 직접 입력
    			String serviceKey = "b545bddf8f7e1ed43c646564c909d4a7";
    			//바인드할 때 서비스 키를 입력받도록 하는 경우
    			//String serviceKey = (String)service_info.get("serviceKey");
    			
    			model.addAttribute("naverSearch_Url", service_info.get("url"));
    			model.addAttribute("naverSearch_Key", serviceKey);    			
    		}	
    	}
        return "main";
    }
}
