package sampleApp.common;

import java.util.Map;

import org.springframework.cloud.app.ApplicationInstanceInfo;
import org.springframework.cloud.app.BasicApplicationInstanceInfo;

/**
 * 개방형 클라우드 플랫폼에서 Application의 정보를 가져오기
 * 
 * @author 안찬영
 *
 * History
 * 2015.7.9 최초개발
 */
public class ApplicationInstanceInfoCreator {
	public ApplicationInstanceInfo createApplicationInstanceInfo(Map<String, Object> applicationInstanceData) {
		String instanceId = (String) applicationInstanceData.get("instance_id");
		String appId = (String) applicationInstanceData.get("name"); 

		return new BasicApplicationInstanceInfo(instanceId, appId, applicationInstanceData);
	}
}