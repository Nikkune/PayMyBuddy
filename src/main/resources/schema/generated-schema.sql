create table transactions (amount float(53) not null, id integer not null auto_increment, receiver_id integer not null, sender_id integer not null, description varchar(255), primary key (id)) engine=InnoDB;
create table user_connections (connection_id integer not null, user_id integer not null) engine=InnoDB;
create table users (id integer not null auto_increment, email varchar(255), password varchar(255), username varchar(255), primary key (id)) engine=InnoDB;
alter table transactions add constraint FK5nn8ird7idyxyxki68gox2wbx foreign key (receiver_id) references users (id);
alter table transactions add constraint FK3ly4r8r6ubt0blftudix2httv foreign key (sender_id) references users (id);
alter table user_connections add constraint FKohvj3bhf0c6gb645k4atn4rax foreign key (connection_id) references users (id);
alter table user_connections add constraint FK56b5yg0vwv72mhph7e5u2hn6x foreign key (user_id) references users (id);
