package org.openpaas.servicebroker.controller;

import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.model.Catalog;
import org.openpaas.servicebroker.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Catalog API 를 호출 받는 컨트롤러이다.
 * 
 * @author 송창학
 * @date 2015.0629
 */

@Controller
public class CatalogController extends BaseController {
	
	public static final String BASE_PATH = "/v2/catalog";
	
	private static final Logger logger = LoggerFactory.getLogger(CatalogController.class);
	
	private CatalogService service;
	
	@Autowired 
	public CatalogController(CatalogService service) {
		this.service = service;
	}
	
	@RequestMapping(value = BASE_PATH, method = RequestMethod.GET)
	public @ResponseBody Catalog getCatalog() throws ServiceBrokerException{
		logger.debug("GET: " + BASE_PATH + ", getCatalog()");
		return service.getCatalog();
	}
	
}
