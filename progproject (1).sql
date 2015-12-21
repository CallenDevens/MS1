-- phpMyAdmin SQL Dump
-- version 4.2.11
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Jun 15, 2015 at 07:12 AM
-- Server version: 5.6.21
-- PHP Version: 5.6.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `progproject`
--

-- --------------------------------------------------------

--
-- Table structure for table `parentnames`
--

CREATE TABLE IF NOT EXISTS `parentnames` (
`pn_id` int(11) NOT NULL,
  `r_id` int(11) DEFAULT NULL,
  `pn_name` varchar(100) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `parentnames`
--

INSERT INTO `parentnames` (`pn_id`, `r_id`, `pn_name`) VALUES
(1, NULL, 'FlexrayArch_nStore'),
(2, 1, 'msg_nStore'),
(3, 2, 'disjointFrame_Lwrong'),
(4, 3, 'lemma_A'),
(5, 4, 'lemma_B'),
(6, 5, 'lemma_C'),
(7, 6, 'lemma_D');

-- --------------------------------------------------------

--
-- Table structure for table `parenttitles`
--

CREATE TABLE IF NOT EXISTS `parenttitles` (
`pt_id` int(11) NOT NULL,
  `pt_title` varchar(100) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `parenttitles`
--

INSERT INTO `parenttitles` (`pt_id`, `pt_title`) VALUES
(1, 'FR_proof');

-- --------------------------------------------------------

--
-- Table structure for table `projects`
--

CREATE TABLE IF NOT EXISTS `projects` (
`p_id` int(11) NOT NULL,
  `p_title` varchar(100) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `projects`
--

INSERT INTO `projects` (`p_id`, `p_title`) VALUES
(1, 'Project A'),
(2, 'Project B'),
(4, 'Project C');

-- --------------------------------------------------------

--
-- Table structure for table `projectsusers`
--

CREATE TABLE IF NOT EXISTS `projectsusers` (
  `p_id` int(11) NOT NULL,
  `u_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `projectsusers`
--

INSERT INTO `projectsusers` (`p_id`, `u_id`) VALUES
(1, 1),
(4, 1),
(1, 3),
(2, 3),
(2, 5);

-- --------------------------------------------------------

--
-- Table structure for table `requests`
--

CREATE TABLE IF NOT EXISTS `requests` (
`r_id` int(11) NOT NULL,
  `r_project` int(11) NOT NULL,
  `r_requester` int(11) NOT NULL,
  `r_reqdate` datetime NOT NULL,
  `r_lemmatitle` varchar(10000) NOT NULL,
  `r_lemma` varchar(10000) NOT NULL,
  `r_parenttitle` int(11) NOT NULL,
  `r_parentname` int(11) NOT NULL,
  `r_status` varchar(25) NOT NULL,
  `r_contributor` int(11) DEFAULT NULL,
  `r_condate` datetime DEFAULT NULL,
  `r_comment` varchar(10000) DEFAULT NULL,
  `r_moddate` datetime NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `requests`
--

INSERT INTO `requests` (`r_id`, `r_project`, `r_requester`, `r_reqdate`, `r_lemmatitle`, `r_lemma`, `r_parenttitle`, `r_parentname`, `r_status`, `r_contributor`, `r_condate`, `r_comment`, `r_moddate`) VALUES
(1, 1, 1, '2015-03-28 00:00:00', 'msg_nStore', '	assumes h1:" forall i<n. FlexRayController (nReturn i)\r\n	recv (nC i) (nStore i) (nSend i) (nGet i)"\r\n		and h2:"DisjointSchedules n nC"\r\n		and h3:"IdenticCycleLength n nC"\r\n		and h4:"inf_disj n nSend"\r\n		and h5:"i < n"  \r\n		and h6:"forall i<n. msg (Suc 0) (nReturn i)"\r\n		and h7:"Cable n nSend recv"\r\n	shows "msg (Suc 0) (nStore i)"', 1, 1, 'IN PROGRESS', 1, '2015-06-10 02:38:10', '', '2015-03-28 00:00:00'),
(2, 1, 3, '2015-04-09 00:00:00', 'disjointFrame_Lwrong', '	assumes h1:"Â¬ DisjointSchedules n nC"  \r\n	and h2:"IdenticCycleLength n nC"\r\n	and h3:"forall i < n. FlexRayController (nReturn i) rcv \r\n		(nC i) (nStore i) (nSend i) (nGet i)"\r\n	shows "inf_disj n nSend"', 1, 1, 'IN PROGRESS', 1, '2015-06-10 04:01:24', '', '2015-04-09 00:00:00'),
(3, 1, 1, '2015-06-09 22:19:04', 'lemma_A', 'this is lemma_A', 1, 1, 'CONTRADICTION', 1, '2015-06-10 02:50:16', 'This lemma is dependent on a lemma with a contradiction', '2015-06-09 22:19:04'),
(4, 1, 1, '2015-06-09 22:19:38', 'lemma_B', 'this is lemma_B', 1, 4, 'IN PROGRESS', 1, '2015-06-10 02:50:21', '', '2015-06-09 22:19:38'),
(5, 1, 1, '2015-06-10 02:37:47', 'lemma_C', 'this is lemma_C', 1, 4, 'CONTRADICTION', 1, '2015-06-10 03:06:24', 'This lemma is dependent on a lemma with a contradiction', '2015-06-10 02:37:47'),
(6, 1, 1, '2015-06-10 03:04:22', 'lemma_D', 'this is lemma_D', 1, 6, 'CONTRADICTION', 1, '2015-06-10 05:01:39', 'This is the contradiction', '2015-06-10 03:04:22');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
`u_id` int(11) NOT NULL,
  `u_email` varchar(35) NOT NULL,
  `u_pw` varchar(50) NOT NULL,
  `u_fname` varchar(25) NOT NULL,
  `u_lname` varchar(25) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`u_id`, `u_email`, `u_pw`, `u_fname`, `u_lname`) VALUES
(1, 'peter@email.com', '5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8', 'Peter', 'Ravindra'),
(3, 'jdoe@email.com', '3da541559918a808c2402bba5012f6c60b27661c', 'John', 'Doe'),
(5, 'jsmith@email.com', 'b1b3773a05c0ed0176787a4f1574ff0075f7521e', 'John', 'Smith');

-- --------------------------------------------------------

--
-- Table structure for table `usersrequests`
--

CREATE TABLE IF NOT EXISTS `usersrequests` (
  `u_id` int(11) NOT NULL,
  `r_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `usersrequests`
--

INSERT INTO `usersrequests` (`u_id`, `r_id`) VALUES
(1, 1),
(3, 1),
(1, 2),
(3, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `parentnames`
--
ALTER TABLE `parentnames`
 ADD PRIMARY KEY (`pn_id`), ADD KEY `r_id` (`r_id`);

--
-- Indexes for table `parenttitles`
--
ALTER TABLE `parenttitles`
 ADD PRIMARY KEY (`pt_id`);

--
-- Indexes for table `projects`
--
ALTER TABLE `projects`
 ADD PRIMARY KEY (`p_id`);

--
-- Indexes for table `projectsusers`
--
ALTER TABLE `projectsusers`
 ADD PRIMARY KEY (`p_id`,`u_id`), ADD KEY `projectsusers_ibfk_2` (`u_id`);

--
-- Indexes for table `requests`
--
ALTER TABLE `requests`
 ADD PRIMARY KEY (`r_id`), ADD KEY `r_contributor` (`r_contributor`), ADD KEY `r_requester` (`r_requester`), ADD KEY `r_project` (`r_project`), ADD KEY `r_parentname` (`r_parentname`), ADD KEY `r_parenttitle` (`r_parenttitle`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
 ADD PRIMARY KEY (`u_id`), ADD UNIQUE KEY `u_email` (`u_email`);

--
-- Indexes for table `usersrequests`
--
ALTER TABLE `usersrequests`
 ADD PRIMARY KEY (`u_id`,`r_id`), ADD KEY `usersrequests_ibfk_2` (`r_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `parentnames`
--
ALTER TABLE `parentnames`
MODIFY `pn_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=8;
--
-- AUTO_INCREMENT for table `parenttitles`
--
ALTER TABLE `parenttitles`
MODIFY `pt_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=2;
--
-- AUTO_INCREMENT for table `projects`
--
ALTER TABLE `projects`
MODIFY `p_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=5;
--
-- AUTO_INCREMENT for table `requests`
--
ALTER TABLE `requests`
MODIFY `r_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=7;
--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
MODIFY `u_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=6;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `parentnames`
--
ALTER TABLE `parentnames`
ADD CONSTRAINT `parentnames_ibfk_1` FOREIGN KEY (`r_id`) REFERENCES `requests` (`r_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `projectsusers`
--
ALTER TABLE `projectsusers`
ADD CONSTRAINT `projectsusers_ibfk_1` FOREIGN KEY (`p_id`) REFERENCES `projects` (`p_id`) ON DELETE CASCADE ON UPDATE CASCADE,
ADD CONSTRAINT `projectsusers_ibfk_2` FOREIGN KEY (`u_id`) REFERENCES `users` (`u_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `requests`
--
ALTER TABLE `requests`
ADD CONSTRAINT `requests_ibfk_1` FOREIGN KEY (`r_requester`) REFERENCES `users` (`u_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
ADD CONSTRAINT `requests_ibfk_3` FOREIGN KEY (`r_contributor`) REFERENCES `users` (`u_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
ADD CONSTRAINT `requests_ibfk_4` FOREIGN KEY (`r_project`) REFERENCES `projects` (`p_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
ADD CONSTRAINT `requests_ibfk_5` FOREIGN KEY (`r_parenttitle`) REFERENCES `parenttitles` (`pt_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
ADD CONSTRAINT `requests_ibfk_6` FOREIGN KEY (`r_parentname`) REFERENCES `parentnames` (`pn_id`) ON DELETE NO ACTION ON UPDATE CASCADE;

--
-- Constraints for table `usersrequests`
--
ALTER TABLE `usersrequests`
ADD CONSTRAINT `usersrequests_ibfk_1` FOREIGN KEY (`u_id`) REFERENCES `users` (`u_id`) ON DELETE NO ACTION ON UPDATE CASCADE,
ADD CONSTRAINT `usersrequests_ibfk_2` FOREIGN KEY (`r_id`) REFERENCES `requests` (`r_id`) ON DELETE CASCADE ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
