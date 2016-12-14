package servicebroker.pinpoiont.service.impl;

import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openpaas.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.ServiceInstanceBinding;
//import org.openpaas.servicebroker.pinpoint.CubridConfiguration;
import org.openpaas.servicebroker.pinpoint.exception.PinpointServiceException;
import org.openpaas.servicebroker.pinpoint.model.PinpointServiceInstance;
import org.openpaas.servicebroker.pinpoint.model.PinpointServiceInstanceBinding;
import org.openpaas.servicebroker.pinpoint.config.CollectorConfig;
import org.openpaas.servicebroker.pinpoint.config.PinpointConfig;
import org.openpaas.servicebroker.pinpoint.exception.PinpointServiceException;
import org.openpaas.servicebroker.pinpoint.model.BindingJson;
import org.openpaas.servicebroker.pinpoint.model.CollectorJson;
import org.openpaas.servicebroker.pinpoint.model.PinpointJson;
import org.openpaas.servicebroker.pinpoint.model.PinpointServiceInstanceBinding;
import org.openpaas.servicebroker.pinpoint.service.impl.PinpointAdminService;
//import org.openpaas.servicebroker.util.JSchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openpaas.servicebroker.pinpoint.config.PinpointConfig.SERVICE_DIR;

@RunWith(SpringJUnit4ClassRunner.class)
@Ignore
public class PinpointAdminServiceTest {
	private final Logger logger = LoggerFactory.getLogger(PinpointAdminService.class);
	private CollectorConfig collectorConfig = new CollectorConfig();
	private PinpointConfig pinpointConfig = new PinpointConfig();

	/**
	 * 요청 정보로 부터 ServiceInstance를 생성합니다.
	 */
	public ServiceInstance createServiceInstanceByRequest(CreateServiceInstanceRequest request) throws PinpointServiceException {
		logger.debug("PinpointAdminService.createServiceInstanceByRequest");
		PinpointJson pinpointJson = pinpointConfig.getPinpointJson();
		List<String> lstPinpointInstance = pinpointJson.getInstance_id();
		String sInstanceId = request.getServiceInstanceId();
		String sDashboardUrl = getDashboardUrl();
		if(isExistInstance(sInstanceId)) throw new PinpointServiceException("already  has been created instance ");
		lstPinpointInstance.add(sInstanceId);
		pinpointJson.setinstance_id(lstPinpointInstance);
		ServiceInstance serviceInstance = new ServiceInstance(request);
		serviceInstance.withDashboardUrl(sDashboardUrl);

		pinpointConfig.writePinpointJson(pinpointJson.toJsonString());
		return serviceInstance;
	}



	public void deleteServiceInstance(String serviceInstanceId) throws PinpointServiceException {
		logger.debug(this.getClass().getName()+":deleteServiceInstance");
		PinpointJson pinpointJson = pinpointConfig.getPinpointJson();
		List<String> lstPinpointInstance = pinpointJson.getInstance_id();
		for (int i =0;i<lstPinpointInstance.size();i++){
			if(serviceInstanceId.equals(lstPinpointInstance.get(i))){
				lstPinpointInstance.remove(i);
			}
		}
		pinpointJson.setinstance_id(lstPinpointInstance);
		pinpointConfig.writePinpointJson(pinpointJson.toJsonString());

	}

	/**
	 * 요청 정보로부터 ServiceInstanceBinding 정보를 생성합니다.
	 */
	public ServiceInstanceBinding createServiceInstanceBindingByRequest(CreateServiceInstanceBindingRequest request) throws PinpointServiceException, ServiceInstanceBindingExistsException {
		logger.debug(this.getClass().getName()+":createServiceInstanceBindingByRequest");

		Map map = request.getParameters();
		CollectorJson collectorJson = collectorConfig.collectorJson();
		Map credential = new HashMap<String, Object>();
		String application_name = (String) map.get("application_name");
		String sBindingId = request.getBindingId();
		PinpointServiceInstanceBinding pinpointServiceInstanceBinding = new PinpointServiceInstanceBinding(
				sBindingId,
				request.getServiceInstanceId(),
				credential,
				"",request.getAppGuid());
		logger.debug("ServiceInstanceBinding/createServiceInstanceBindingByRequest/" + map.toString());
		if(isExistBinding(sBindingId)) throw new ServiceInstanceBindingExistsException(pinpointServiceInstanceBinding);
		if (null == application_name) throw new PinpointServiceException("Must be support cf -c {\"application_name\": \"parameter\"}");

		if (application_name.length() > 24) {
			application_name = application_name.substring(0, 23);
		}
		PinpointJson pinpointJson = pinpointConfig.getPinpointJson();

		String collectorIp = getCollectorIp(collectorJson);
		int collectorSpanPort = Integer.parseInt(collectorJson.getCollector_span_port());
		int collectorStatPort = Integer.parseInt(collectorJson.getCollector_stat_port());
		int collectorTcpPort = Integer.parseInt(collectorJson.getCollector_tcp_port());

		credential.put("collector_host", collectorIp);
		credential.put("collector_span_port", collectorSpanPort);
		credential.put("collector_stat_port", collectorStatPort);
		credential.put("collector_tcp_port", collectorTcpPort);
//			credential.put("dashboard_url", applcation_name);
		credential.put("agent_id", application_name);
		credential.put("application_name", application_name);


		List<BindingJson> lstBindingJson = pinpointJson.getBinding();
		BindingJson bindingJson = new BindingJson();
		bindingJson.setAgentId(application_name);
		bindingJson.setApplication_name(application_name);
		bindingJson.setBinding_id(sBindingId);
		bindingJson.setCollector_ip(collectorIp);
		bindingJson.setInstance_url(application_name);
		bindingJson.setInstance_id(request.getServiceInstanceId());

		lstBindingJson.add(bindingJson);
		pinpointJson.setBinding(lstBindingJson);

		pinpointConfig.writePinpointJson(pinpointJson.toJsonString());
		logger.debug("lstBindingJson:"+pinpointJson.toJsonString());
		ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding(request.getBindingId(), request.getServiceInstanceId(), credential,
				"syslogDrainUrl", request.getAppGuid());
		return serviceInstanceBinding;
	}

	public ServiceInstanceBinding deleteServiceInstanceBinding(String bindingId) throws PinpointServiceException{
		PinpointJson pinpointJson = pinpointConfig.getPinpointJson();
		List<BindingJson> lstBinding = pinpointJson.getBinding();
		ServiceInstanceBinding serviceInstanceBinding =new PinpointServiceInstanceBinding();
		for (int i = 0;i<lstBinding.size();i++){
			BindingJson bindingJson = lstBinding.get(i);
			if(bindingId.equals(bindingJson.getBinding_id())){
				lstBinding.remove(i);
				Gson gson = new Gson();
				String jsonString = gson.toJson(bindingJson);
				logger.debug(this.getClass().getName()+": deleteServiceInstanceBinding :"+jsonString);
				serviceInstanceBinding.setServiceInstanceId(bindingJson.getInstance_id());
				serviceInstanceBinding.setId(bindingJson.getBinding_id());
			}
		}
		pinpointJson.setBinding(lstBinding);
		pinpointConfig.writePinpointJson(pinpointJson.toJsonString());
		return serviceInstanceBinding;
	}


	private PinpointServiceException handleException(Exception e) {
		logger.warn(e.getLocalizedMessage(), e);
		return new PinpointServiceException(e.getLocalizedMessage());
	}

	/**
	 * service instance 개수를 알아냄.
	 *
	 * @return
	 */
	public int getInstanceTot() throws PinpointServiceException {
		logger.debug("PinpointAdminService.getInstanceTot");
		int iFileTot = 0;
		try {
			Gson gson = new Gson();

			File fileDir = new File("/" + SERVICE_DIR);
			iFileTot = fileDir.listFiles().length;
		} catch (Exception e) {
			handleException(e);
		}
		return iFileTot;
	}

	// DashboardUrl 생성
	public String getDashboardUrl() throws PinpointServiceException {
		String sDashboardUrl = "";
		try {
			CollectorJson collectorJson = collectorConfig.collectorJson();
			sDashboardUrl = collectorJson.getDashboard_url();
		} catch (Exception e) {
			new PinpointServiceException(e.getLocalizedMessage());
		}
		return sDashboardUrl;
	}

	/**
	 * 서비스 존재 여부를 확인합니다.
	 *
	 * @param instanceId
	 * @return
	 */
	public boolean isExistInstance(String instanceId) throws PinpointServiceException{
		logger.debug(this.getClass().getName()+":isExistInstance");
		PinpointJson pinpointJson = pinpointConfig.getPinpointJson();
		boolean bRtn = false;
		List<String> list = pinpointJson.getInstance_id();
		if(null!=list){
			for (int i=0;i<list.size();i++){
				if(list.get(i).toString().equals(instanceId)){
					bRtn = true;
				}
			}
		}
		return bRtn;
	}

	/**
	 * Exist Bindoing
	 *
	 * @param bindingId
	 * @return
	 */
	public boolean isExistBinding(String bindingId) throws PinpointServiceException {
		logger.debug("PinpointAdminService.isExistsBinding");

		boolean bRtn = false;
		PinpointJson pinpointJson = pinpointConfig.getPinpointJson();
		List<BindingJson> lstBinding = pinpointJson.getBinding();
		for (int i=0;i<lstBinding.size();i++){
			BindingJson bindingJson=lstBinding.get(i);
			if(bindingId.equals(bindingJson.getBinding_id())){
				bRtn = true;
			}
		}
		return bRtn;
	}

	public String getCollectorIp(CollectorJson collectorJson) throws PinpointServiceException{
		String rtnCollectorIp ="";
		PinpointJson pinpointJson = pinpointConfig.getPinpointJson();
		List<String> lstCIp= collectorJson.getCollector_ips();
		List<BindingJson> lstBndIp= pinpointJson.getBinding();

		logger.debug(pinpointJson.toJsonString());
		int iLstCIp = lstCIp.size();
		int iLstBndIP = lstBndIp.size();
		logger.debug("iLstCIp:"+iLstCIp);
		logger.debug("iLstBndIP:"+iLstBndIP);

		long lAvg = 0;
		List<Long> lstLIP = new ArrayList<Long>();

		if (0== iLstBndIP) {  lAvg = 1; }else{ lAvg = iLstBndIP/iLstCIp;}
		for (int i=0; i < lstCIp.size(); i++) {
			String sCollectorIp = lstCIp.get(i);
			long lavg = 0;
			if (null != lstBndIp) {
				for (int j = 0; j < lstBndIp.size(); j++) {
					BindingJson bindingJson = lstBndIp.get(j);
					String collectorIp = bindingJson.getCollector_ip();
					if(sCollectorIp.equals(collectorIp)){
						lavg = lavg + 1;
					}
				}
			}

			logger.debug("lavg:"+i+":"+lavg+"\\n");
			lstLIP.add(lavg);

		}
		int cnt = 0;
		while(cnt < lstCIp.size()){
			logger.debug("lstLIP.get(cnt)<lAvg:"+lstLIP.get(cnt)+":"+lAvg+"\\n");
			if(lstLIP.get(cnt)<=lAvg ){
				logger.debug("lstLIP.get(cnt)<lAvg:"+lstCIp.get(cnt)+":"+lstCIp.get(cnt)+"\\n");
				rtnCollectorIp = lstCIp.get(cnt);
				break;
			}
			cnt++;
		}

		logger.debug("rtnCollectorIp:"+rtnCollectorIp+"\\n");
		if("".equals(rtnCollectorIp)){
			rtnCollectorIp = lstCIp.get(0);
		}
		return rtnCollectorIp;
	}


}
