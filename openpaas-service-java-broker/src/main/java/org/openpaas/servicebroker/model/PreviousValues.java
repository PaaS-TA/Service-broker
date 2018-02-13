package org.openpaas.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * PreviousValues 정보를 가지고 있는 데이터 모델 bean 클래스.
 * Json 어노테이션을 사용해서 JSON 형태로 제공
 *
 * @author lena
 * @date 2017.05.23
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreviousValues {

    @JsonSerialize
    @JsonProperty("plan_id")
    private String planId;

    @JsonSerialize
    @JsonProperty("service_id")
    private String serviceDefinitionId;

    @JsonSerialize
    @JsonProperty("organization_id")
    private String organizationId;

    @JsonSerialize
    @JsonProperty("space_id")
    private String spaceId;

    public PreviousValues() {}

    public PreviousValues(String planId, String serviceDefinitionId, String organizationId, String spaceId) {
        this.planId = planId;
        this.serviceDefinitionId = serviceDefinitionId;
        this.organizationId = organizationId;
        this.spaceId = spaceId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getServiceDefinitionId() {
        return serviceDefinitionId;
    }

    public void setServiceDefinitionId(String serviceDefinitionId) {
        this.serviceDefinitionId = serviceDefinitionId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }
}
