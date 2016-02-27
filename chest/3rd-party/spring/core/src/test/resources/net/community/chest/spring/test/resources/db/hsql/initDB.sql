CREATE TABLE date_time_entity (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1),
	version INTEGER NOT NULL,
	name VARCHAR(255) UNIQUE NOT NULL,
	description VARCHAR(80),
	date_value TIMESTAMP(2) WITHOUT TIME ZONE NOT NULL,

	CONSTRAINT date_time_entity_pk PRIMARY KEY(id)
);

CREATE INDEX date_time_entity_name_idx ON date_time_entity(name);

CREATE TABLE node_entity (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1),
	version INTEGER NOT NULL,
	name VARCHAR(255) UNIQUE NOT NULL,
	description VARCHAR(80),
	parent_id BIGINT,
	child_index INTEGER,

	CONSTRAINT node_entity_pk PRIMARY KEY(id),
	FOREIGN KEY (parent_id) REFERENCES node_entity(id)
);

CREATE INDEX node_entity_name_idx ON node_entity(name);

CREATE TABLE embedding_entity (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1),
	version INTEGER NOT NULL,
	name VARCHAR(255) UNIQUE NOT NULL,
	description VARCHAR(80),

	CONSTRAINT embedding_entity_pk PRIMARY KEY(id)
);

CREATE TABLE embedded_instances (
	owner_id BIGINT NOT NULL,
	name VARCHAR(128),
	address VARCHAR(80),

	FOREIGN KEY (owner_id) REFERENCES embedding_entity(id)
);


