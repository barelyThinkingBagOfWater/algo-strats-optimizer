db = db.getSiblingDB('quotes');

db.createUser(
   {
     user: "quotes-importer",
     pwd: "quotes-importer123",
     roles: [ {role: "readWrite", db: "quotes"} ]
   }
);
db.createUser(
   {
     user: "backtester",
     pwd: "backtester123",
     roles: [ {role: "read", db: "quotes"} ]
   }
);
db.createUser(
   {
     user: "tradingbot",
     pwd: "tradingbot123",
     roles: [ {role: "read", db: "quotes"} ]
   }
);

db = db.getSiblingDB('results');

db.createUser(
   {
     user: "backtester",
     pwd: "backtester123",
     roles: [ {role: "readWrite", db: "results"} ]
   }
);
