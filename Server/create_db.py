import sqlite3

db = sqlite3.connect('fingerprints.db')

db.execute('''
CREATE TABLE IF NOT EXISTS Fingerprints (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    image BLOB
)
''')

insert = "INSERT INTO Fingerprints (id, image) VALUES (?, ?)"

fp = None
with open ('fingerprints/leftIndex.bmp', 'rb') as file:
    fp = file.read()

data = (0, fp)

db.execute(insert, data)


db.commit()
db.close()