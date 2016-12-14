/**
 * pinpoint 서비스 관련 에러 Exception클래스
 */
package org.openpaas.servicebroker.pinpoint.exception;

import org.openpaas.servicebroker.exception.ServiceBrokerException;

public class PinpointServiceException extends ServiceBrokerException {

    private static final long serialVersionUID = 8667141725171626000L;

    public PinpointServiceException(String message) {
        super(message);
    }

}
