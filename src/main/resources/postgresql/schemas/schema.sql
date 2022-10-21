begin;

create table if not exists Authors(
    name varchar(20) primary key,
    email varchar(255) constraint emailinvalid check(email ~* '^[A-Z0-9.%-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,4}$'),
    github varchar(255)
);

create table if not exists SystemInfo(
    name varchar(20) primary key,
    version varchar(20)
);

create table if not exists "User" (
  id serial primary key,
  "name" varchar(20) unique not null
);

create table if not exists WaitingLobby(
    id serial primary key,
    userID int,
    foreign key (userID) references "User"(id)
);



create table if not exists token(
    token varchar(255) primary key,
    userID int,
    foreign key(userID) references "User"(id)
);

create table if not exists ShipRules(
    id serial primary key,
    fleetInfo jsonb
);

create table if not exists GameRules (
    id serial primary key,
    boardSide int,
    shotsPerTurn int,
    layoutDefinitionTimeout int,
    playTimeout int,
    shiprules int,
    foreign key(shiprules) references ShipRules(id)
);

create table if not exists Game (
    id serial primary key,
    rules int, foreign key(rules) references GameRules(id),
    "state" varchar(20) check ("state" like 'placing_ships' or "state" like 'playing' or "state" like 'finished'),
    turn int,
    player1 int, foreign key(player1) references "User"(id),
    player2 int, foreign key(player2) references "User"(id),
    winner int, foreign key(winner) references "User"(id),
    foreign key(turn) references "User"(id)
);

create table if not exists Board (
    layout text,
    gameId int,
    userId int,
    primary key (gameId, userId),
    foreign key (gameId) references Game(id),
    foreign key (userId) references "User"(id)
);
commit;




