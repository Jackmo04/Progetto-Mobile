use master
drop database geotag;
create database geotag;
use geotag;


create table utente (
	ute_id int IDENTITY(1,1) primary key,
	ute_username varchar(50) not null,
	ute_password varchar(50) not null,
	ute_immagine varchar(100) not null
);

create table partita (
	par_id int IDENTITY(1,1) primary key,
	par_organizzatore int not null,
	par_latitudine float not null,
	par_longitudine float not null,
	par_data datetime not null,
	par_descrizione VARCHAR(100) null,
	foreign key (par_organizzatore) references utente(ute_id)
);

create table partecipazione (
	prt_partita int not null,
	prt_utente int not null,
	foreign key (prt_partita) references partita(par_id),
	foreign key (prt_utente) references utente(ute_id),
	primary key (prt_partita, prt_utente)
);

create table tag (
	tag_id int IDENTITY(1,1) primary key,
	tag_posizione int not null,
	tag_partita int not null,
	tag_hash varchar(50) not null,
	tag_latitudine float not null,
	tag_longitudine float not null,
	tag_descrizione varchar(100) not null,
	tag_immagine varchar(100) not null,
    foreign key (tag_partita) references partita(par_id)
);

-- Created by GitHub Copilot in SSMS - review carefully before executing
-- Inserimento dati d'esempio sulle tabelle dbo.utente e dbo.partita


-- 1. Inserimento Utenti
INSERT INTO dbo.utente (ute_username, ute_password, ute_immagine)
VALUES ('mattia.cavina2@studio.unibo.it', 'pwd123', 'default.png');

INSERT INTO dbo.utente (ute_username, ute_password, ute_immagine)
VALUES ('matteo.grandini@studio.unibo.it', 'pwd123', 'default.png');

INSERT INTO dbo.utente (ute_username, ute_password, ute_immagine)
VALUES ('giuliabianchi@example.it', 'pwd123', 'default.png');

INSERT INTO dbo.utente (ute_username, ute_password, ute_immagine)
VALUES ('annaneri@example.it', 'pwd123', 'default.png');

-- 2. Inserimento Partite con l'id effettivo appena creato
INSERT INTO dbo.partita (par_organizzatore, par_latitudine, par_longitudine, par_data)
VALUES 
    (1, 45.4642, 9.1900,  DATEADD(day, -2, GETDATE())), -- Milano
    (1, 41.9028, 12.4964, DATEADD(day, -1, GETDATE())), -- Roma
    (2, 43.7696, 11.2558, DATEADD(day, -3, GETDATE())), -- Firenze
    (3, 45.4384, 10.9916, DATEADD(day, -1, GETDATE())), -- Verona
    (4, 40.8518, 14.2681, GETDATE());                   -- Napoli

-- 3 Creare delle partecipazioni 
INSERT INTO dbo.partecipazione( prt_partita, prt_utente) values
(1,2),
(1,3),
(1,4),
(2,1),
(2,3),
(3,4),
(3,1);

--4 Aggiunta tag

-- Created by GitHub Copilot in SSMS - review carefully before executing
-- Creazione di 5 tag generati in prossimità della partita (±0.01 gradi).

-- Creiamo una tabella comune (CTE) per replicare 5 righe (i tag)
WITH Nums AS (
    SELECT 1 AS n UNION ALL 
    SELECT 2 UNION ALL 
    SELECT 3 UNION ALL 
    SELECT 4 UNION ALL 
    SELECT 5
)
INSERT INTO dbo.tag (
    tag_posizione,
    tag_partita,
    tag_hash,
    tag_latitudine,
    tag_longitudine,
    tag_descrizione,
    tag_immagine
)
SELECT 
    n.n AS tag_posizione,
    p.par_id AS tag_partita,
    -- Genera un Hash casuale fittizio e univoco
    CONCAT('HASH_', p.par_id, '_', n.n, '_', LEFT(CAST(NEWID() AS VARCHAR(50)), 8)) AS tag_hash,
    -- Offset geolocalizzazione radiale usando NEWID + CHECKSUM (± 1km circa/0.01 deg)
    p.par_latitudine + ((ABS(CHECKSUM(NEWID())) % 200) - 100) / 10000.0 AS tag_latitudine,
    p.par_longitudine + ((ABS(CHECKSUM(NEWID())) % 200) - 100) / 10000.0 AS tag_longitudine,
    CONCAT('Tag n.', n.n, ' - Partita ', p.par_id) AS tag_descrizione,
    'tag_default.png' AS tag_immagine
FROM 
    dbo.partita p
CROSS JOIN 
    Nums n;
