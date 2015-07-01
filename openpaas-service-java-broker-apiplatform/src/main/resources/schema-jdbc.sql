CREATE TABLE IF NOT EXISTS apiplatform_users(
organization_guid VARCHAR(255) NOT NULL,
user_id VARCHAR(30) NOT NULL,
user_password VARCHAR(255) NOT NULL,
createtimestamp DATETIME NOT NULL,
PRIMARY KEY (organization_guid),
UNIQUE (user_id),
UNIQUE(organization_guid)
);
CREATE TABLE IF NOT EXISTS apiplatform_services(
organization_guid VARCHAR(255) NOT NULL,
instance_id VARCHAR(255) NOT NULL,
space_guid VARCHAR(255) NOT NULL,
service_id VARCHAR(255) NOT NULL,
plan_id VARCHAR(255) NOT NULL,
delyn CHAR(1) NOT NULL,
createtimestamp DATETIME NOT NULL,
deletetimestamp DATETIME,
PRIMARY KEY(organization_guid,instance_id),
UNIQUE (instance_id)
)