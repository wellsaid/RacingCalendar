DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS series;

DROP TABLE IF EXISTS session_types;
DROP TABLE IF EXISTS series_types;

CREATE TABLE series_types (
	short_name VARCHAR(20) PRIMARY KEY,
	complete_name VARCHAR(50),
	description VARCHAR(500),
	thumbnail_URL VARCHAR(100)
);

CREATE TABLE series (
	short_name VARCHAR(10) PRIMARY KEY,
	complete_name VARCHAR(50) NOT NULL,
	series_type VARCHAR(30) NOT NULL,
	description VARCHAR(300),
	logo_URL VARCHAR(100),
	thumbnail_URL VARCHAR(100),
	FOREIGN KEY (series_type) REFERENCES series_types(short_name)
);

CREATE TABLE events (
	ID INTEGER,
	series_short_name VARCHAR(10),
	event_short_name VARCHAR(10),
	event_name VARCHAR(50),
	circuit_name VARCHAR(50),
	start_date DATE,
	end_date DATE,
	FOREIGN KEY (series_short_name) REFERENCES series(short_name),
	PRIMARY KEY (ID, series_short_name)
);

CREATE TABLE session_types (
	short_name VARCHAR(10) PRIMARY KEY,
	complete_name VARCHAR(50) NOT NULL
);

/* TODO create session table */