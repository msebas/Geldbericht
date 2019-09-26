-- MySQL dump 10.16  Distrib 10.2.25-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: geldberichtDb
-- ------------------------------------------------------
-- Server version	10.2.25-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Accounts`
--

DROP TABLE IF EXISTS `Accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Accounts` (
  `uid` bigint(20) NOT NULL,
  `lastChange` datetime DEFAULT NULL,
  `accountName` varchar(255) DEFAULT NULL,
  `accountNumber` varchar(255) DEFAULT NULL,
  `balance` decimal(19,2) DEFAULT NULL,
  `initialBalance` decimal(19,2) DEFAULT NULL,
  `company_uid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  KEY `FKc6ghoyd8rj69uf1junwbjvfth` (`company_uid`),
  CONSTRAINT `FKc6ghoyd8rj69uf1junwbjvfth` FOREIGN KEY (`company_uid`) REFERENCES `Companies` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Companies`
--

DROP TABLE IF EXISTS `Companies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Companies` (
  `uid` bigint(20) NOT NULL,
  `lastChange` datetime DEFAULT NULL,
  `companyBookkeepingAppointment` varchar(255) DEFAULT NULL,
  `companyName` varchar(256) DEFAULT NULL,
  `companyNumber` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `MonthAccountTurnovers`
--

DROP TABLE IF EXISTS `MonthAccountTurnovers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MonthAccountTurnovers` (
  `uid` bigint(20) NOT NULL,
  `lastChange` datetime DEFAULT NULL,
  `finalAssets` decimal(19,2) DEFAULT NULL,
  `finalDebt` decimal(19,2) DEFAULT NULL,
  `initialAssets` decimal(19,2) DEFAULT NULL,
  `initialDebt` decimal(19,2) DEFAULT NULL,
  `month` date DEFAULT NULL,
  `monthBalanceAssets` decimal(19,2) DEFAULT NULL,
  `monthBalanceDebt` decimal(19,2) DEFAULT NULL,
  `account_uid` bigint(20) DEFAULT NULL,
  `monthBlocked` bit(1) NOT NULL,
  PRIMARY KEY (`uid`),
  KEY `FKdm4uv0ytebiied0xj17j6neha` (`account_uid`),
  CONSTRAINT `FKdm4uv0ytebiied0xj17j6neha` FOREIGN KEY (`account_uid`) REFERENCES `Accounts` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `MonthAccountTurnovers_Transactions`
--

DROP TABLE IF EXISTS `MonthAccountTurnovers_Transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MonthAccountTurnovers_Transactions` (
  `MonthAccountTurnover_uid` bigint(20) NOT NULL,
  `transactions_uid` bigint(20) NOT NULL,
  UNIQUE KEY `UK_3dhvqm50why8gwlx89rrdhh80` (`transactions_uid`),
  KEY `FK4gwhet74dwyr0rary6xp6dd57` (`MonthAccountTurnover_uid`),
  CONSTRAINT `FK4gwhet74dwyr0rary6xp6dd57` FOREIGN KEY (`MonthAccountTurnover_uid`) REFERENCES `MonthAccountTurnovers` (`uid`),
  CONSTRAINT `FKhle0a35g763yopgjl5k4qk5m4` FOREIGN KEY (`transactions_uid`) REFERENCES `Transactions` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Transactions`
--

DROP TABLE IF EXISTS `Transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Transactions` (
  `uid` bigint(20) NOT NULL,
  `lastChange` datetime DEFAULT NULL,
  `accountingContraAccount` int(11) DEFAULT NULL,
  `accountingCostCenter` int(11) DEFAULT NULL,
  `accountingCostGroup` int(11) DEFAULT NULL,
  `descriptionOfTransaction` varchar(255) DEFAULT NULL,
  `inventoryNumber` varchar(255) DEFAULT NULL,
  `number` int(11) NOT NULL,
  `receipts` decimal(19,2) DEFAULT NULL,
  `spending` decimal(19,2) DEFAULT NULL,
  `transactionDate` date DEFAULT NULL,
  `voucher` varchar(255) DEFAULT NULL,
  `vat_uid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  KEY `FKbdmr253js8kcqp7bc4j7py09q` (`vat_uid`),
  CONSTRAINT `FKbdmr253js8kcqp7bc4j7py09q` FOREIGN KEY (`vat_uid`) REFERENCES `VatTypes` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Users`
--

DROP TABLE IF EXISTS `Users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Users` (
  `uid` bigint(20) NOT NULL,
  `lastChange` datetime(6) DEFAULT NULL,
  `passwordHash` varchar(255) DEFAULT NULL,
  `userName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Users`
--

LOCK TABLES `Users` WRITE;
/*!40000 ALTER TABLE `Users` DISABLE KEYS */;
INSERT INTO `Users` VALUES (1,'2019-08-14 18:29:54.000000','$argon2i$v=19$m=996401,t=2,p=6$+kbRaP0/xsH0koB6Pr9w6A$u14M0YdDawqYWtFS4kU7aeJGYRRUV86toEHfpIq239M','admin');
/*!40000 ALTER TABLE `Users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `VatTypes`
--

DROP TABLE IF EXISTS `VatTypes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `VatTypes` (
  `uid` bigint(20) NOT NULL,
  `lastChange` datetime DEFAULT NULL,
  `defaultVatType` bit(1) DEFAULT NULL,
  `disabledVatType` bit(1) DEFAULT NULL,
  `name` varchar(256) DEFAULT NULL,
  `shortName` varchar(4) DEFAULT NULL,
  `value` decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` VALUES (1);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-09-26 16:58:17
