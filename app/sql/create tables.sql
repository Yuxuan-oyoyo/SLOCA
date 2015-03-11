
use app;
create table locationlookup (
locationId varchar(128) not null,
semanticPlace varchar(128) not null,
constraint loclookup_pk primary key (locationId)
);

create table user (
macAddress varchar(128) not null,
name varchar(128) not null,
password varchar(128) not null,
email varchar(128) not null,
gender varchar(128) not null,
constraint user_pk primary key (macAddress)

);

create table location(
id int not null auto_increment,
timeStamp timestamp not null,
macAddress varchar(128) not null,
locationID varchar(128) not null,
constraint loc_pk primary key (id, timeStamp, macAddress)
);

