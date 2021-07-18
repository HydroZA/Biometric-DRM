import sqlite3

db = sqlite3.connect('fingerprints.db')

db.execute('PRAGMA foreign_keys = 1')

# Create software table -- holds data about the software which this drm protects --
db.execute('''
CREATE TABLE IF NOT EXISTS Software (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT,
    developer TEXT,
    releaseyear INT
)
''')

# Create fingerprints table
db.execute('''
CREATE TABLE IF NOT EXISTS Fingerprints (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    image BLOB
)
''')

# create license table, maps fingerprints to licenses
db.execute('''
CREATE TABLE IF NOT EXISTS Licenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fingerprint INT,
    software INT,
    FOREIGN KEY (fingerprint) REFERENCES Fingerprints(id)
    FOREIGN KEY (software) REFERENCES Software(id)
)
''')

def create_fingerprints_table():
    insert = "INSERT INTO Fingerprints (id, image) VALUES (?, ?)"

    fp = None
    with open ('fingerprints/leftIndex.bmp', 'rb') as file:
        fp = file.read()
    data = (1, fp)
    db.execute(insert, data)

    fp = None
    with open ('fingerprints/NatLeftIndex.bmp', 'rb') as file:
        fp = file.read()
    data = (2, fp)
    db.execute(insert, data)

    fp = None
    with open ('fingerprints/RightIndex.bmp', 'rb') as file:
        fp = file.read()

    data = (3, fp)
    db.execute(insert, data)

def create_software_table():
    sql = "INSERT INTO Software (title, developer, releaseyear) VALUES (?, ?, ?)"

    software = []
    software.append(('Doom', 'id Software', 1993))
    software.append(('IntelliJ IDEA', 'JetBrains', 2001))
    software.append(('WordPerfect', 'Novell', '1979'))
    software.append(('The Witcher 3: Wild Hunt', 'CD Projekt RED', 2015))
    software.append(('Firefox', 'Mozilla', 2002))

    for s in software:
        db.execute(sql, s)

def create_licenses_table():
    sql = "INSERT INTO Licenses (fingerprint, Software) VALUES (?, ?)"

    licenses = []
    licenses.append((1, 2))
    licenses.append((3, 1))
    licenses.append((1, 4))
    licenses.append((1, 3))
    licenses.append((2, 3))

    for l in licenses:
        db.execute(sql, l)

create_fingerprints_table()
create_software_table()
db.commit()

create_licenses_table()
db.commit()

db.close()