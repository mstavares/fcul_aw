CREATE DATABASE wa;
USE wa;

CREATE TABLE diseases (
	id INT NOT NULL AUTO_INCREMENT, 
	name VARCHAR(255) NOT NULL, 
	abstract VARCHAR(255), 
	was_derived_from VARCHAR(255), 
	PRIMARY KEY (id)
)ENGINE=InnoDB; 

CREATE TABLE images (
	id INT NOT NULL AUTO_INCREMENT,
	url VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
)ENGINE=InnoDB;

CREATE TABLE tweets (
	id INT NOT NULL AUTO_INCREMENT,
	url VARCHAR(255) NOT NULL,
	text VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
)ENGINE=InnoDB;

CREATE TABLE pubmed (
	id INT NOT NULL AUTO_INCREMENT,
	title VARCHAR(255) NOT NULL,
	abstract VARCHAR(255),
	PRIMARY KEY (id)
)ENGINE=InnoDB;

CREATE TABLE diseases_images (
	id_diseases INT NOT NULL,
	id_images INT NOT NULL,
	black_listed BOOLEAN DEFAULT false,
	PRIMARY KEY (id_diseases, id_images),
	FOREIGN KEY (id_diseases) REFERENCES diseases(id) ON UPDATE CASCADE,
	FOREIGN KEY (id_images) REFERENCES images(id) ON UPDATE CASCADE
)ENGINE=InnoDB;

CREATE TABLE diseases_tweets (
	id_diseases INT NOT NULL,
	id_tweets INT NOT NULL,
	black_listed BOOLEAN DEFAULT false,
	PRIMARY KEY (id_diseases, id_tweets),
	FOREIGN KEY (id_diseases) REFERENCES diseases(id) ON UPDATE CASCADE,
	FOREIGN KEY (id_tweets) REFERENCES tweets(id) ON UPDATE CASCADE
);

CREATE TABLE diseases_pubmed (
	id_diseases INT NOT NULL,
	id_pubmed INT NOT NULL,
	PRIMARY KEY (id_diseases, id_pubmed),
	FOREIGN KEY (id_diseases) REFERENCES diseases(id) ON UPDATE CASCADE,
	FOREIGN KEY (id_pubmed) REFERENCES pubmed(id) ON UPDATE CASCADE
);



