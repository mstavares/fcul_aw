CREATE TABLE diseases (
	id INT NOT NULL AUTO_INCREMENT, 
	doid VARCHAR(20),
	name VARCHAR(255) UNIQUE NOT NULL, 
	abstract MEDIUMTEXT, 
	was_derived_from VARCHAR(255),
	field VARCHAR(64),
	death_cause_of VARCHAR(255),
	idf DOUBLE DEFAULT 0,
	PRIMARY KEY (id)
)ENGINE=InnoDB; 

CREATE TABLE images (
	id INT NOT NULL AUTO_INCREMENT,
	url VARCHAR(255) UNIQUE NOT NULL,
	PRIMARY KEY (id)
)ENGINE=InnoDB;

CREATE TABLE tweets (
	id INT NOT NULL AUTO_INCREMENT,
	url VARCHAR(255) UNIQUE NOT NULL,
	text VARCHAR(255) NOT NULL,
	pub_date DATE,
	id_original_disease INT NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (id_original_disease) REFERENCES diseases(id) ON UPDATE CASCADE ON DELETE CASCADE
)ENGINE=InnoDB;

CREATE TABLE pubmed (
	id INT NOT NULL AUTO_INCREMENT,
	pubmedID INT UNIQUE NOT NULL,
	title VARCHAR(1024) NOT NULL,
	abstract MEDIUMTEXT,
	pub_date DATE,
	id_original_disease INT NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (id_original_disease) REFERENCES diseases(id) ON UPDATE CASCADE ON DELETE CASCADE
)ENGINE=InnoDB;

CREATE TABLE diseases_images (
	id_diseases INT NOT NULL,
	id_images INT NOT NULL,
	implicit_feedback INT DEFAULT 0,
	explicit_feedback INT DEFAULT 0,
	PRIMARY KEY (id_diseases, id_images),
	FOREIGN KEY (id_diseases) REFERENCES diseases(id) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (id_images) REFERENCES images(id) ON UPDATE CASCADE ON DELETE CASCADE
)ENGINE=InnoDB;

CREATE TABLE diseases_tweets (
	id_diseases INT NOT NULL,
	id_tweets INT NOT NULL,
	implicit_feedback INT DEFAULT 0,
	explicit_feedback INT DEFAULT 0,
	rank DOUBLE DEFAULT 0,
	PRIMARY KEY (id_diseases, id_tweets),
	FOREIGN KEY (id_diseases) REFERENCES diseases(id) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (id_tweets) REFERENCES tweets(id) ON UPDATE CASCADE ON DELETE CASCADE
)ENGINE=InnoDB;

CREATE TABLE diseases_pubmed (
	id_diseases INT NOT NULL,
	id_pubmed INT NOT NULL,
	implicit_feedback INT DEFAULT 0,
	explicit_feedback INT DEFAULT 0,
	rank DOUBLE DEFAULT 0,
	occurrences INT NOT NULL,
	places VARCHAR(255),
	tf DOUBLE DEFAULT 0,
	PRIMARY KEY (id_diseases, id_pubmed),
	FOREIGN KEY (id_diseases) REFERENCES diseases(id) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (id_pubmed) REFERENCES pubmed(id) ON UPDATE CASCADE ON DELETE CASCADE
)ENGINE=InnoDB;