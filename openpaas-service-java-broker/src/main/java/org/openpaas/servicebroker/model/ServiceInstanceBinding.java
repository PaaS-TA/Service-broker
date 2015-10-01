package org.openpaas.servicebroker.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 서비스 인스턴스 바인드된 정보를 가지고 있는 데이터 모델 bean 클래스. 
 * Json 어노테이션을 사용해서 JSON 형태로 제공
 * 
 * @author 송창학
 * @date 2015.0629
 */
public class ServiceInstanceBinding {

	private String id;
	private String serviceInstanceId;
	private Map<String,Object> credentials = new HashMap<String,Object>();
	private String syslogDrainUrl;
	private String appGuid;
	
	@JsonIgnore
	private HttpStatus httpStatus = HttpStatus.CREATED;
	
	public ServiceInstanceBinding(String id, 
			String serviceInstanceId, 
			Map<String,Object> credentials,
			String syslogDrainUrl, String appGuid) {
		this.id = id;
		this.serviceInstanceId = serviceInstanceId;
		setCredentials(credentials);
		this.syslogDrainUrl = syslogDrainUrl;
		this.appGuid = appGuid;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public Map<String, Object> getCredentials() {
		return credentials;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	private void setCredentials(Map<String, Object> credentials) {
		if (credentials == null) {
			credentials = new HashMap<String,Object>();
		} else {
			this.credentials = credentials;
		}
	}

	public String getSyslogDrainUrl() {
		return syslogDrainUrl;
	}
	
	public String getAppGuid() {
		return appGuid;
	}
	
	public void setHttpStatusOK(){
		this.httpStatus=HttpStatus.OK;
	}
	
	public HttpStatus getHttpStatus(){
		return httpStatus;
	}
	
}
