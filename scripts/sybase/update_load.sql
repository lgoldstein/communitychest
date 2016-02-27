--
-- Update from ITCA-SE 1.0/ITCA-SE 1.0 HF1 => ITCA-SE 1.1
--
-- ( Version :  10.0.1.3579)
--
-- Script revision $Rev: 561 $
--
--


-------------------------------------------------
--   Create missing tables
-------------------------------------------------
-- create log messages import table
if not exists (select 1 from dbo.sysobjects where type='U' and name = 'LogMessageEntry')
   CREATE TABLE "itcase"."LogMessageEntry" (
         "id"            numeric(19,0) NOT NULL DEFAULT autoincrement ,
         "version"       integer NULL ,
         "name"          varchar(255) NOT NULL ,
         "creationTime"  "datetime" NOT NULL ,
         "logLevel"      varchar(32) NOT NULL ,
         "logThread"     varchar(255) NOT NULL ,
         "logPackage"    varchar(255) NULL ,
         "logClass"      varchar(255) NULL ,
         "logNDCUnique"  varchar(255) NULL ,
         "logMessage"    varchar(1024) NOT NULL ,
         "logException"  varchar(1024) NULL ,
         "logCause"      varchar(1024) NULL ,
         PRIMARY KEY ("id")
   )
go

-- Add the compliance tracking table
if not exists (select 1 from dbo.sysobjects where type='U' and name = 'ComplianceInProgress')
	CREATE TABLE "itcase"."ComplianceInProgress" (
	     "policyRuleId"   numeric(19,0) NOT NULL  ,
	      "startedAt" "datetime" NOT NULL DEFAULT CURRENT DATE,
	      PRIMARY KEY ("policyRuleId")
      )
go

-------------------------------------------------
-- update new columns
-------------------------------------------------

-- IF NOT EXISTS (SELECT 1 FROM dbo.sysobjects AS so,dbo.syscolumns AS sc
--                       WHERE so.id = sc.id
--                         AND so.type = 'U'
--                         AND so.name = 'Role'
--                         AND sc.name = 'name' )
--     ALTER TABLE "itcase"."Role" ADD name varchar(255);
--     UPDATE "itcase"."Role" set "itcase"."Role"."name" = 'Security Administrator' WHERE "itcase"."Role"."globalPermission" = 'SECURITY_ADMIN';
--     UPDATE "itcase"."Role" set "itcase"."Role"."name" = 'Administrator'          WHERE "itcase"."Role"."globalPermission" = 'ADIMINISTRATOR';
--     UPDATE "itcase"."Role" set "itcase"."Role"."name" = 'User'                   WHERE "itcase"."Role"."globalPermission" = 'USER';
--     UPDATE "itcase"."Role" set "itcase"."Role"."name" = 'Guest'                  WHERE "itcase"."Role"."globalPermission" = 'GUEST';
--     COMMIT;
--     ALTER TABLE "itcase"."Role" MODIFY name NOT NULL;
-- go

-------------------------------------------------
--   Set md_info READONLY and CATALOG to return false
-------------------------------------------------
update "dbo"."spt_mda" set "dbo"."spt_mda"."querytype" = 4, "dbo"."spt_mda"."query" = '0'
where "dbo"."spt_mda"."mdinfo" = 'SET_READONLY_FALSE' ;
update "dbo"."spt_mda" set "dbo"."spt_mda"."querytype" = 4, "dbo"."spt_mda"."query" = '0'
where "dbo"."spt_mda"."mdinfo" = 'SET_READONLY_TRUE' ;
update "dbo"."spt_mda" set "dbo"."spt_mda"."querytype" = 4, "dbo"."spt_mda"."query" = '0'
where "dbo"."spt_mda"."mdinfo" = 'SET_CATALOG' ;
commit
go

-------------------------------------------------
--   Table data changes
-------------------------------------------------
-- if not exists (select 1 from itcase.ConfigValue where id=12)
--   insert into "itcase"."ConfigValue" (id,version,name,containerGroup,minValue,maxValue,valueSource,valueType,description,valueString)
--   values (12,0,'GDPM',8,0.0,255.0,'DEFAULT','STRING','Comma separated names of GDPM DM(s)','gdpm');
-- commit
-- go

-- add mapping masking event to rule 27 (masking w/o mapping)
if not exists (select 1 from itcase.Rule_ElementProperty where elementProperties_id=7 and Rule_id=27)
   insert into "itcase"."Rule_ElementProperty" ("Rule_id","elementProperties_id") values (27, 7)
commit
go

-- remove mapping masking event to rule 22 (iopath redundancy)
if exists (select 1 from itcase.Rule_ElementProperty where elementProperties_id=7 and Rule_id=22)
   delete from "itcase"."Rule_ElementProperty" where elementProperties_id=7 and Rule_id=22
commit
go

-- corrections to element properties
update "itcase"."ElementProperty" set "itcase"."ElementProperty"."name"='adapters_update' where id=4
commit
go

if not exists (select 1 from itcase.Rule_ElementProperty where elementProperties_id=33 and Rule_id=50)
   insert into "itcase"."Rule_ElementProperty" ("Rule_id","elementProperties_id") values (50, 33)
commit
go

if not exists (select 1 from itcase.Rule_ElementProperty where Rule_id=28)
   insert into "itcase"."Rule_ElementProperty" ("Rule_id","elementProperties_id") values (28, 27)
   insert into "itcase"."Rule_ElementProperty" ("Rule_id","elementProperties_id") values (28, 10)
   insert into "itcase"."Rule_ElementProperty" ("Rule_id","elementProperties_id") values (28, 11)
commit
go

if not exists (select 1 from itcase.Rule_ElementProperty where  Rule_id=9)
   insert into "itcase"."Rule_ElementProperty" ("Rule_id","elementProperties_id") values (9, 1)
commit
go

if not exists (select 1 from itcase.Rule_ElementProperty where  Rule_id=26)
   insert into "itcase"."Rule_ElementProperty" ("Rule_id","elementProperties_id") values (26, 1)
commit
go

-- Update role descriptions strings
update "itcase"."Role" set "itcase"."Role"."description"='Security Administrator' where id=1
update "itcase"."Role" set "itcase"."Role"."description"='Administrator' where id=2
update "itcase"."Role" set "itcase"."Role"."description"='User' where id=3
update "itcase"."Role" set "itcase"."Role"."description"='Guest' where id=4
commit
go
