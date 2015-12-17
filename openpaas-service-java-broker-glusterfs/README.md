# openpaas-service-java-broker-glusterfs

#DB Schema
CREATE DATABASE IF NOT EXISTS gfbroker;
USE gfbroker;
CREATE TABLE IF NOT EXISTS `service_binding` (
  `binding_id` varchar(100) NOT NULL DEFAULT '',
  `instance_id` varchar(100) DEFAULT NULL,
  `username` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `app_id` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`binding_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS  `service_instances` (
  `instance_id` varchar(100) NOT NULL,
  `service_id` varchar(100) NOT NULL,
  `plan_id` varchar(100) NOT NULL,
  `organization_guid` varchar(100) NOT NULL,
  `space_guid` varchar(100) NOT NULL,
  `tenant_name` varchar(100) DEFAULT NULL,
  `tenant_id` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;