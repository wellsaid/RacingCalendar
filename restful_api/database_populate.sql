DELETE FROM events;
DELETE FROM session_types;
DELETE FROM series;
DELETE FROM series_types;

INSERT INTO series_types VALUES (
	  'moto_circuit',
	  'Motorbike Circuit Racing',
	  NULL,
	  'https://upload.wikimedia.org/wikipedia/commons/6/60/MotoGP_final_race.jpg'
);

INSERT INTO series_types VALUES (
	  'formula',
	  'Formula Open Wheel Racing',
	  NULL,
	  'https://upload.wikimedia.org/wikipedia/commons/6/69/Mansell_cart.jpg'
);

INSERT INTO series VALUES (
	  'motogp',
	  'Moto Grand Prix',
	  'moto_circuit',
	  'Premier class of motorcycle racing eventss held on road circuits sanctioned by FIM.',
	  'https://upload.wikimedia.org/wikipedia/commons/a/a0/Moto_Gp_logo.svg',
	  'https://upload.wikimedia.org/wikipedia/commons/6/60/MotoGP_final_race.jpg'
);

INSERT INTO series VALUES (
	  'f1',
	  'FIA Formula 1',
	  'formula',
	  'The highest class of single-seater auto racing sanctioned by the (FIA) ',
	  'https://upload.wikimedia.org/wikipedia/en/4/45/F1_logo.svg',
	  'https://upload.wikimedia.org/wikipedia/commons/1/14/2010_Malaysian_GP_opening_lap.jpg'
);

INSERT INTO events VALUES(
	1,
	'motogp',
	'qatar',
	'Grand Prix of Qatar',
	'Losail International Circuit',
	'2018-03-16',
	'2018-03-18'
);

INSERT INTO events VALUES(
	2,
	'motogp',
	'argentina',
	'Gran Premio Motul de la República Argentina',
	'Termas de Río Hondo',
	'2018-04-06',
	'2018-04-08'
);

INSERT INTO events VALUES(
	3,
	'motogp',
	'texas',
	'Red Bull Grand Prix of The Americas',
	'Circuit Of The Americas',
	'2018-04-20',
	'2018-04-22'
);

INSERT INTO events VALUES(
	4,
	'motogp',
	'jerez',
	'Gran Premio Red Bull de Espana',
	'Circuito de Jerez - Angel Nieto',
	'2018-05-04',
	'2018-05-06'
);

INSERT INTO events VALUES(
	5,
	'motogp',
	'le_mans',
	'HJC Helmets Grand Prix de France',
	'Le Mans',
	'2018-05-18',
	'2018-05-20'
);

INSERT INTO events VALUES(
	6,
	'motogp',
	'mugello',
	'Gran Premio d\'Italia Oakley',
	'Autodromo del Mugello',
	'2018-06-01',
	'2018-06-03'
);

INSERT INTO events VALUES(
	7,
	'motogp',
	'catalunya',
	'Gran Premi Monster Energy de Catalunya',
	'Circuit de Barcelona-Catalunya',
	'2018-07-15',
	'2018-07-17'
);

INSERT INTO events VALUES(
	8,
	'motogp',
	'assen',
	'Motul TT Assen',
	'TT Circuit Assen',
	'2018-06-29',
	'2018-07-01'
);

INSERT INTO events VALUES(
	9,
	'motogp',
	'deutsch',
	'Pramac Motorrad Grand Prix Deutschland',
	'Sachsenring',
	'2018-08-13',
	'2018-08-15'
);

INSERT INTO events VALUES(
	10,
	'motogp',
	'brno',
	'Monster Energy Grand Prix Ceske republiky',
	'Automotodrom Brno',
	'2018-08-03',
	'2018-08-05'
);

INSERT INTO events VALUES(
	11,
	'motogp',
	'rbr',
	'eyetime Motorrad Grand Prix von Osterreich',
	'Red Bull Ring – Spielberg',
	'2018-09-10',
	'2018-09-12'
);

INSERT INTO events VALUES(
	12,
	'motogp',
	'uk',
	'GoPro British Grand Prix',
	'Silverstone Circuit',
	'2018-08-24',
	'2018-08-26'
);

INSERT INTO events VALUES(
	13,
	'motogp',
	'san_marino',
	'Gran Premio Octo di San Marino',
	'Misano World Circuit Marco Simoncelli',
	'2018-09-07',
	'2018-09-09'
);

INSERT INTO events VALUES(
	14,
	'motogp',
	'aragon',
	'Gran Premio Movistar de Aragón',
	'MotorLand Aragon',
	'2018-09-21',
	'2018-09-23'
);

INSERT INTO events VALUES(
	15,
	'motogp',
	'thailand',
	'PTT Thailand Grand Prix',
	'Chang International Circuit',
	'2018-10-05',
	'2018-10-07'
);

INSERT INTO events VALUES(
	16,
	'motogp',
	'motegi',
	'Motul Grand Prix of Japan',
	'Twin Ring Motegi',
	'2018-10-19',
	'2018-10-21'
);

INSERT INTO events VALUES(
	17,
	'motogp',
	'philip_isl',
	'Michelin Australian Motorcycle Grand Prix',
	'Phillip Island',
	'2018-10-26',
	'2018-10-28'
);

INSERT INTO events VALUES(
	18,
	'motogp',
	'sepang',
	'Shell Malaysia Motorcycle Grand Prix',
	'Sepang International Circuit',
	'2018-11-02',
	'2018-11-04'
);

INSERT INTO events VALUES(
	19,
	'motogp',
	'valencia',
	'Gran Premio Motul de la Comunitat Valenciana',
	'Circuit Ricardo Tormo',
	'2018-11-16',
	'2018-11-18'
);

/* TODO: Add moto gp sessions */

/* TODO: Add f1 events */

/* TODO: Add f1 sessions */

INSERT INTO session_types VALUES('fp','Free Practice');
INSERT INTO session_types VALUES('qp','Qualyfing Practice');
INSERT INTO session_types VALUES('rac','Race');