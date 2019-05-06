/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  jacek
 * Created: 2019-05-03
 */
CREATE SCHEMA IF NOT EXISTS my_base;
DROP TABLE IF EXISTS my_base.CUSTOMERS;
CREATE TABLE my_base.CUSTOMERS (
    ID bigint(100) not null 
    ,NAME varchar(100) not null default '' 
    ,SURNAME varchar(100) not null default '' 
    ,AGE int
    ,CITY varchar(100) not null default '' 
    ,PRIMARY KEY(ID)
);

DROP TABLE IF EXISTS my_base.CONTACT;
CREATE TABLE my_base.CONTACTS (
	ID bigint(100) not null 
    ,ID_CUSTOMER bigint(100) not null 
    ,TYPE int not null  default 0 
    ,CONTACT varchar(200) not null  default '' 
    ,PRIMARY KEY (ID)
    ,KEY FK_CUSTOMER_ID_idx (ID_CUSTOMER)
    ,CONSTRAINT FK_CUSTOMER_ID FOREIGN KEY (ID_CUSTOMER) REFERENCES my_base.CUSTOMERS (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);


