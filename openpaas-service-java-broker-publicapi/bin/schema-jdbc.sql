CREATE TABLE IF NOT EXISTS publicapi_services(
organization_guid VARCHAR(255) NOT NULL,
instance_id VARCHAR(255) NOT NULL,
space_guid VARCHAR(255) NOT NULL,
service_id VARCHAR(255) NOT NULL,
plan_id VARCHAR(255) NOT NULL,
service_key VARCHAR(255),
delyn CHAR(1) NOT NULL,
createtimestamp DATETIME NOT NULL,
deletetimestamp DATETIME,
PRIMARY KEY(organization_guid, instance_id),
UNIQUE (instance_id)
);
CREATE TABLE IF NOT EXISTS publicapi_bindings(
binding_id VARCHAR(255) NOT NULL,
instance_id VARCHAR(255) NOT NULL,
app_guid VARCHAR(255) NOT NULL,
delyn CHAR(1) NOT NULL,
createtimestamp DATETIME NOT NULL,
deletetimestamp DATETIME,
PRIMARY KEY(binding_id)
)