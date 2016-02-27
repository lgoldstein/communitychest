-- Populate the role enumeration values
INSERT INTO LutUserRoleType (name) VALUES ('ADMIN');
INSERT INTO LutUserRoleType (name) VALUES ('CUSTOMER');
INSERT INTO LutUserRoleType (name) VALUES ('GUEST');

-- Add some default users - we set our own ID(s) in order to generate correct associations
INSERT INTO User (id,name,loginName,password,role,homeAddress,latitude,longitude)
			VALUES (1,'Administrator','admin','admin','ADMIN','Sapir 3, Herzliya, Israel', '32.1638822', '34.811586499999976');
INSERT INTO User (id,name,loginName,password,role,homeAddress,latitude,longitude)
			VALUES (2,'Customer','customer','customer','CUSTOMER','1600 Pennsylvania Ave NW, Washington, DC 20502','38.8976777','-77.036517');
INSERT INTO User (id,name,loginName,password,role,homeAddress,latitude,longitude)
			VALUES (3,'Guest','guest','guest','GUEST', 'Palacio de la Moncloa, Madrid, Spain','40.444555','-3.7359199999999646');

-- Add some banks + branches + accounrs - we set our own ID(s) in order to generate correct associations
INSERT INTO Bank (id,name,bankCode,hqAddress,latitude,longitude)
			VALUES (10,'Leumi',10,'Yehuda Halevi 24, Tel Aviv, Israel','32.0615007','34.770387400000004');
INSERT INTO Branch (id,name,bankId,branchCode,location)
			VALUES (681,'Ramat Poleg',10,681,'Zalman Shazar 10, Netanya, Israel');
INSERT INTO Branch (id,name,bankId,branchCode,location)
			VALUES (953,'Hadera',10,953,'Herbert Samuel 58, Hadera, Israel');
INSERT INTO Branch (id,name,bankId,branchCode,location)
			VALUES (976,'Qiryat Shemona',10,976,'Kikar Tsahal, Qiryat Shemona, Israel');

INSERT INTO Bank (id,name,bankCode,hqAddress,latitude,longitude)
			VALUES (12,'Ha-Poalim',12,'Sderot Rothschild 50, Tel-Aviv, Israel','32.0640343','34.77516390000005');
INSERT INTO Branch (id,name,bankId,branchCode,location)
			VALUES (629,'Herzliya Pituah',12,629,'Ha-Maapilim 39, Herzliya, Israel');
INSERT INTO Branch (id,name,bankId,branchCode,location)
			VALUES (644,'Eilat',12,644,'Hativat HaNegev Ave 3, Eilat, Israel');
INSERT INTO Branch (id,name,bankId,branchCode,location)
			VALUES (705,'Hertzel',12,705,'Hertsel 73, Haifa, Israel');

INSERT INTO Bank (id,name,bankCode,hqAddress,latitude,longitude)
			VALUES (20,'Mizrahi-Tfahot',20,'Jabotinsky 7, Ramat Gan, Israel','32.0830813','34.80398190000005');
INSERT INTO Branch (id,name,bankId,branchCode,location)
			VALUES (445,'Akko',20,445,'Ben Ami 47, Acre, Israel');
INSERT INTO Branch (id,name,bankId,branchCode,location)
			VALUES (462,'Tveria',20,462,'Ha-Banim, Tiberias, Israel');
INSERT INTO Branch (id,name,bankId,branchCode,location)
			VALUES (464,'Keren Ha-Yesod',20,464,'Keren HaYesod 130, Beersheba, Israel');

INSERT INTO Account (ownerId,branchId,accountNumber,amount)
			VALUES (2,629,'71241',7365);
INSERT INTO Account (ownerId,branchId,accountNumber,amount)
			VALUES (2,445,'3777347',260212);
INSERT INTO Account (ownerId,branchId,accountNumber,amount)
			VALUES (2,976,'1243567',260212);
