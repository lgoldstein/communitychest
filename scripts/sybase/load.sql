--
-- This command file reloads a database that was unloaded using "dbunload".
--
-- ( Version :  10.0.1.3579)
--
-- Script revision $Rev: 561 $
--
--


SET OPTION date_order          = 'YMD'
go

SET OPTION PUBLIC.preserve_source_format = 'OFF'
go

SET TEMPORARY OPTION tsql_outer_joins = 'ON'
go


-------------------------------------------------
--   Create dbspaces
-------------------------------------------------


-------------------------------------------------
--   Create users
-------------------------------------------------
GRANT CONNECT,DBA,RESOURCE TO "itcase" IDENTIFIED BY emcfiji
go


GRANT CONNECT,DBA,RESOURCE TO "dba" IDENTIFIED BY sql
go

GRANT CONNECT,DBA,GROUP,RESOURCE TO "dbo"
go


-------------------------------------------------
--   Create user types
-------------------------------------------------


-------------------------------------------------
--   Create group memberships
-------------------------------------------------


-------------------------------------------------
--   Create remote servers
-------------------------------------------------


-------------------------------------------------
--   Create tables
-------------------------------------------------

CREATE TABLE "itcase"."Breach" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"creationTime"                   "datetime" NOT NULL DEFAULT CURRENT DATE
   ,"lastModificationTime"           "datetime" NULL
   ,"acknowledgedByUserProfileId"    numeric(19,0) NULL
   ,"acknowledgedTime"               "datetime" NULL
   ,"classification"                 varchar(32) NOT NULL
   ,"closedTime"                     "datetime" NULL
   ,"description"                    varchar(1024) NULL
   ,"lastCheckedTime"                "datetime" NULL
   ,"policyRuleId"                   numeric(19,0) NOT NULL
   ,"priority"                       varchar(32) NOT NULL
   ,"recommendation"                 varchar(1024) NULL
   ,"sourceElementId"                numeric(19,0) NULL
   ,"modifiedByUserId"               numeric(19,0) NULL
   ,"paramViolationInfo_id"          numeric(19,0) NULL
   ,"parent_id"                      numeric(19,0) NULL
   ,"hasChildren"                    tinyint NOT NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."Breach_changeUids" (
    "Breach_id"                      numeric(19,0) NOT NULL
   ,"element"                        varchar(36) NULL
)
go

CREATE TABLE "itcase"."Breach_closeChangeUids" (
    "Breach_id"                      numeric(19,0) NOT NULL
   ,"element"                        varchar(36) NULL
)
go

CREATE TABLE "itcase"."PendingBreach" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"creationTime"                   "datetime" NOT NULL
   ,"lastModificationTime"           "datetime" NULL
   ,"acknowledgedByUserProfileId"    numeric(19,0) NULL
   ,"acknowledgedTime"               "datetime" NULL
   ,"classification"                 varchar(32) NOT NULL
   ,"closedTime"                     "datetime" NULL
   ,"description"                    varchar(1024) NULL
   ,"hasChildren"                    tinyint NULL
   ,"lastCheckedTime"                "datetime" NULL
   ,"modifiedByUserId"               numeric(19,0) NULL
   ,"policyRuleId"                   numeric(19,0) NOT NULL
   ,"priority"                       varchar(32) NOT NULL
   ,"recommendation"                 varchar(1024) NULL
   ,"sourceElementId"                numeric(19,0) NULL
   ,"activationDate"                 "datetime" NOT NULL
   ,"parent_id"                      numeric(19,0) NULL
   ,"paramViolationInfo_id"          numeric(19,0) NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."PendingBreach_changeUids" (
    "PendingBreach_id"               numeric(19,0) NOT NULL
   ,"element"                        varchar(36) NULL
)
go

CREATE TABLE "itcase"."PendingBreach_elements" (
    "PendingBreach_id"               numeric(19,0) NOT NULL
   ,"element"                        numeric(19,0) NULL
)
go

CREATE TABLE "itcase"."ParamViolationInfo" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"actualValue"                    varchar(1024) NOT NULL
   ,"desiredStateValue"              varchar(1024) NOT NULL
   ,"operator"                       varchar(32) NOT NULL
   ,"paramDefId"                     numeric(19,0) NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."Breach_elements" (
    "Breach_id"                      numeric(19,0) NOT NULL
   ,"element"                        numeric(19,0) NULL
)
go

CREATE TABLE "itcase"."ChangeEvent" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"changeType"                     varchar(32) NOT NULL
   ,"creationTime"                   "datetime" NOT NULL DEFAULT CURRENT DATE
   ,"description"                    varchar(1024) NOT NULL
   ,"objectName"                     varchar(255) NOT NULL
   ,"sourceDm"                       varchar(32) NULL
   ,"objectType"                     varchar(255) NOT NULL
   ,"userProfileId"                  numeric(19,0) NULL
   ,"changeUid"											 varchar(36) NULL
   ,PRIMARY KEY CLUSTERED("id")
)
go

CREATE TABLE "itcase"."ChangeEvent_Info" (
    "ChangeEvent_id"                 numeric(19,0) NOT NULL
   ,"info_value"                     varchar(2048) NULL
   ,"info_name"                      varchar(255) NOT NULL
   ,PRIMARY KEY ("ChangeEvent_id","info_name")
)
go

CREATE TABLE "itcase"."Criteria" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"displayName"                    varchar(255) NULL
   ,"dmNameId"                       varchar(255) NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."Criteria_description" (
    "Criteria_id"                    numeric(19,0) NOT NULL
   ,"element"                        varchar(255) NULL
)
go

CREATE TABLE "itcase"."Element" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"creationTime"                   "datetime" NOT NULL DEFAULT CURRENT DATE
   ,"name"                           varchar(255) NOT NULL
   ,"dmNameId"                       varchar(255) NOT NULL
   ,"sourceDm"                       varchar(32) NOT NULL
   ,"type"                           varchar(255) NOT NULL
   ,PRIMARY KEY ("id")
   ,CONSTRAINT "DuplicateElementConstraint" UNIQUE ( "sourceDm","type","dmNameId" )
)
go

CREATE TABLE "itcase"."ElementProperty" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"sourceDm"                       varchar(32) NOT NULL
   ,"type"                           varchar(255) NOT NULL
   ,PRIMARY KEY ("id")
   ,CONSTRAINT "DuplicateElementPropertyConstraint" UNIQUE ( "sourceDm","type","name" )
)
go

CREATE TABLE "itcase"."EmailSettings" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"enabled"                        tinyint NOT NULL
   ,"host"                           varchar(255) NOT NULL UNIQUE
   ,"port"                           integer NOT NULL CONSTRAINT "EmailAlertPortConstraint" check((port > 0) and (port < 65535))
   ,"emailPassword"                  varchar(255) NULL
   ,"emailUser"                      varchar(255) NULL
   ,"systemSenderAddress"            varchar(255) NULL
   ,PRIMARY KEY ("id")
   ,CONSTRAINT "EmailAlertCredentialsConstraint" check(((emailPassword is null) and (emailUser is null)) or ((emailPassword is not null) and (emailUser is not null)))
)
go

CREATE TABLE "itcase"."LdapSettings" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"ADServer"                       varchar(255) NULL
   ,"baseDn"                         varchar(2048) NOT NULL
   ,"domainName"                     varchar(255) NOT NULL
   ,"port"                           integer NOT NULL CONSTRAINT "LdapSettingsPortConstraint" check((port > 0) and (port < 65535))
   ,"server"                         varchar(255) NOT NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."LdapUserDnPattern" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"pattern"                        varchar(2048) NOT NULL
   ,"ldapSettings"                   numeric(19,0) NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."Note" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"creationTime"                   "datetime" NOT NULL DEFAULT CURRENT DATE
   ,"lastModificationTime"           "datetime" NULL
   ,"text"                           varchar(1024) NOT NULL
   ,"lastModifiedBy_id"              numeric(19,0) NULL
   ,"breach"                         numeric(19,0) NULL
   ,"createdBy_id"                   numeric(19,0) NULL
   ,"changeEvent"                    numeric(19,0) NULL
   ,PRIMARY KEY ("id")
   ,CONSTRAINT "NoteReferenceConstraint" check(((breach is null) and (changeEvent is not null)) or ((breach is not null) and (changeEvent is null)))
   ,CONSTRAINT "NoteModificationConstraint" check((lastModifiedBy_id is null) or ((lastModifiedBy_id is not null) and (lastModificationTime is not null)))
)
go

CREATE TABLE "itcase"."ParameterDefinition" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"desiredState"                   varchar(1024) NULL
   ,"desiredStateMouseOver"          varchar(1024) NULL
   ,"operator"          	     varchar(32) NULL
   ,"type"                           varchar(255) NULL
   ,"minValue"                       double NULL
   ,"maxValue"                       double NULL
   ,"parameterDefinition_id"         numeric(19,0) NULL
   ,"exampleText"                    varchar(1024) NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."ParameterDefinition_allowedValues" (
    "ParameterDefinition_id"         numeric(19,0) NOT NULL
   ,"element"                        varchar(255) NULL
)
go

CREATE TABLE "itcase"."ParameterValue" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"enableStatus"                   varchar(32) NOT NULL
   ,"name"                           varchar(255) NOT NULL
   ,"value"                          varchar(255) NOT NULL
   ,"policyRule_id"                  numeric(19,0) NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."Policy" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"creationTime"                   "datetime" NOT NULL DEFAULT CURRENT DATE
   ,"lastModificationTime"           "datetime" NULL
   ,"templateType"                   varchar(32) NOT NULL
   ,"description"                    varchar(255) NULL
   ,"enableStatus"                   varchar(32) NOT NULL
   ,"templateId"                     numeric(19,0) NULL
   ,"lastModifiedBy_id"              numeric(19,0) NULL
   ,"notificationSettings_id"        numeric(19,0) NULL
   ,"createdBy_id"                   numeric(19,0) NULL
   ,"complianceInfo_id"              numeric(19,0) NULL
   ,PRIMARY KEY ("id")
   ,CONSTRAINT "PolicyModificationConstraint" check((lastModifiedBy_id is null) or ((lastModifiedBy_id is not null) and (lastModificationTime is not null)))
   ,CONSTRAINT "DuplicatePolicyConstraint" UNIQUE ( "name", "templateType" )
)
go

CREATE TABLE "itcase"."PolicyGroup" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"creationTime"                   "datetime" NOT NULL DEFAULT CURRENT DATE
   ,"lastModificationTime"           "datetime" NULL
   ,"description"                    varchar(255) NULL
   ,"createdBy_id"                   numeric(19,0) NULL
   ,"lastModifiedBy_id"              numeric(19,0) NULL
   ,PRIMARY KEY ("id")
   ,CONSTRAINT "PolicyGroupModificationConstraint" check((lastModifiedBy_id is null) or ((lastModifiedBy_id is not null) and (lastModificationTime is not null)))
)
go

CREATE TABLE "itcase"."PolicyGroup_Policy" (
    "PolicyGroup_id"                 numeric(19,0) NOT NULL
   ,"Policy_id"                      numeric(19,0) NOT NULL
)
go

CREATE TABLE "itcase"."PolicyNotificationSettings" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"SNMPTrapsEnabled"               tinyint NOT NULL
   ,"emailEnabled"                   tinyint NOT NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."PolicyNotificationSettings_UserProfile" (
    "PolicyNotificationSettings_id"  numeric(19,0) NOT NULL
   ,"notifyUsersList_id"             numeric(19,0) NOT NULL
)
go

CREATE TABLE "itcase"."PolicyNotificationSettings_additionalEmails" (
    "PolicyNotificationSettings_id"  numeric(19,0) NOT NULL
   ,"element"                        varchar(255) NULL
)
go

CREATE TABLE "itcase"."PolicyNotificationSettings_runScripts" (
    "PolicyNotificationSettings_id"  numeric(19,0) NOT NULL
   ,"element"                        varchar(255) NULL
)
go

CREATE TABLE "itcase"."ComplianceInfo" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"complianceStatus"               varchar(32) NULL
   ,"lastCheckedTime"                "datetime" NULL
   ,"breachCount"               		 numeric(19,0) NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."PolicyRule" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"creationTime"                   "datetime" NOT NULL DEFAULT CURRENT DATE
   ,"lastModificationTime"           "datetime" NULL
   ,"templateType"                   varchar(32) NOT NULL
   ,"description"                    varchar(1024) NULL
   ,"enableStatus"                   varchar(32) NOT NULL
   ,"lastCheckVersion"               numeric(19,0) NULL
   ,"recommendation"                 varchar(1024) NULL
   ,"templateId"                     numeric(19,0) NULL
   ,"criteria_id"                    numeric(19,0) NULL
   ,"rule_id"                        numeric(19,0) NULL
   ,"policy"                         numeric(19,0) NULL
   ,"createdBy_id"                   numeric(19,0) NULL
   ,"lastModifiedBy_id"              numeric(19,0) NULL
   ,"classification"                 varchar(32) NULL
   ,"priority"                       varchar(32) NULL
   ,"ruleClass"                      varchar(32) NULL
   ,PRIMARY KEY ("id")
   ,CONSTRAINT "PolicyRuleModificationConstraint" check((lastModifiedBy_id is null) or ((lastModifiedBy_id is not null) and (lastModificationTime is not null)))
)
go

CREATE TABLE "itcase"."Policy_Scope" (
    "Policy_id"                      numeric(19,0) NOT NULL
   ,"scopes_id"                      numeric(19,0) NOT NULL
)
go

CREATE TABLE "itcase"."PolicyRetention" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"enabled"                        tinyint NOT NULL
   ,"objectType"                     varchar(32) NOT NULL UNIQUE
   ,"retentionTimeUnit"              varchar(32) NOT NULL
   ,"retentionTimeValue"             integer NOT NULL CONSTRAINT "RetentionTimeValueConstraint" check(retentionTimeValue >= 0)
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."DbScheduler" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"day"                            varchar(32) NOT NULL
   ,"dbOperation"                    varchar(32) NOT NULL
   ,"enabled"                        tinyint NOT NULL
   ,"hour"                           integer NOT NULL CONSTRAINT "DbScheduledHourConstraint" check((hour >= 0) and (hour <= 23))
   ,"minute"                         integer NOT NULL CONSTRAINT "DbScheduledMinuteConstraint" check((minute >= 0) and (minute <= 59))
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE itcase.DbBackupOnce(
     "id" 			numeric(19,0) NOT NULL DEFAULT autoincrement
    ,"hour" 			int NOT NULL
    ,"minute" 			int NOT NULL
    ,"startDate"		date NOT NULL
    ,"eventName"		varchar(120) null
    ,PRIMARY KEY ( "id" ASC )
)
go

CREATE TABLE "itcase"."Role" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"description"                    varchar(255) NULL
   ,"enableStatus"                   varchar(32) NOT NULL
   ,"globalPermission"               varchar(32) NOT NULL
   ,"objPermission"                  varchar(32) NOT NULL
   ,"authPrivList"                   varchar(1024) NOT NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."Rule" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"breachMessage"                  varchar(1024) NULL
   ,"classification"                 varchar(32) NOT NULL
   ,"description"                    varchar(1024) NULL
   ,"investigateMessage"             varchar(1024) NULL
   ,"priority"                       varchar(32) NOT NULL
   ,"recommendation"                 varchar(1024) NULL
   ,"ruleClass"                      varchar(32) NOT NULL
   ,"ruleLogicClass"                 varchar(255) NULL
   ,"criteria_id"                    numeric(19,0) NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."Rule_ElementProperty" (
    "Rule_id"                        numeric(19,0) NOT NULL
   ,"elementProperties_id"           numeric(19,0) NOT NULL
)
go

CREATE TABLE "itcase"."Scope" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"description"                    varchar(255) NULL
   ,"displayName"                    varchar(255) NULL
   ,"dmNameId"                       varchar(255) NULL
   ,"type"                           varchar(255) NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."SnmpTrapDestination" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"enabled"                        tinyint NOT NULL
   ,"host"                           varchar(255) NOT NULL UNIQUE
   ,"port"                           integer NOT NULL CONSTRAINT "SnmpTrapDestPortConstraint" check((port > 0) and (port < 65535))
   ,"community"                      varchar(255) NOT NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."UserProfile" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"authenticationType"             varchar(32) NOT NULL
   ,"email"                          varchar(255) NOT NULL
   ,"enableStatus"                   varchar(32) NOT NULL
   ,"firstName"                      varchar(45) NOT NULL
   ,"lastName"                       varchar(45) NOT NULL
   ,"nick"                           varchar(45) NOT NULL UNIQUE
   ,"objPermission"                  varchar(32) NOT NULL
   ,"password"                       varchar(255) NULL
   ,"lastLoginTime"                  "datetime" NULL
   ,"lastLogoutTime"                 "datetime" NULL
   ,PRIMARY KEY ("id")
)
go

CREATE TABLE "itcase"."UserProfile_Role" (
    "UserProfile_id"                 numeric(19,0) NOT NULL
   ,"roles_id"                       numeric(19,0) NOT NULL
)
go

CREATE TABLE "itcase"."ConfigGroup" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"creationTime"                   "datetime" NOT NULL DEFAULT CURRENT DATE
   ,"lastModificationTime"           "datetime" NULL
   ,"modifiedByUserId"               numeric(19,0) NULL
   ,"parentGroup"                    numeric(19,0) NOT NULL
   ,"description"                    varchar(1024) NOT NULL
   ,PRIMARY KEY ("id")
   ,CONSTRAINT "DuplicateConfigGroupConstraint" UNIQUE ( "name", "parentGroup" )
)
go

CREATE TABLE "itcase"."ConfigValue" (
    "id"                             numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"                        integer NULL
   ,"name"                           varchar(255) NOT NULL
   ,"creationTime"                   "datetime" NOT NULL DEFAULT CURRENT DATE
   ,"lastModificationTime"           "datetime" NULL
   ,"modifiedByUserId"               numeric(19,0) NULL
   ,"containerGroup"                 numeric(19,0) NOT NULL
   ,"minValue"                       double NOT NULL DEFAULT 0
   ,"maxValue"                       double NOT NULL DEFAULT 0
   ,"valueSource"                    varchar(32) NOT NULL
   ,"valueType"                      varchar(32) NOT NULL
   ,"description"                    varchar(1024) NOT NULL
   ,"valueString"                    varchar(1024) NOT NULL
   ,PRIMARY KEY ("id")
   ,CONSTRAINT "DuplicateConfigValueConstraint" UNIQUE ( "name", "containerGroup", "valueSource" )
)
go

CREATE TABLE "dbo"."maint_plan" (
    "plan_id" unsigned int NOT NULL DEFAULT autoincrement,
    "plan_name" varchar(128) NOT NULL UNIQUE,
    "event_name" varchar(128) NULL,
    "disable_new_connections" bit NOT NULL,
    "disconnect_all_users" bit NOT NULL,
    "do_validate" bit NOT NULL,
    "validate_database_check" bit NOT NULL,
    "validate_checksum_check" bit NOT NULL,
    "validate_express_check" bit NOT NULL,
    "validate_normal_check" bit NOT NULL,
    "do_backup" bit NOT NULL,
    "disk_backup" bit NOT NULL,
    "full_backup" bit NOT NULL,
    "archive_backup" bit NOT NULL,
    "backup_path" long varchar NULL,
    "tape_backup_prompt" bit NOT NULL,
    "tape_backup_comment" long varchar NULL,
    "save_report_count" integer NULL,
    "report_to_console" bit NOT NULL,
    "email_success" bit NOT NULL,
    "email_failure" bit NOT NULL,
    "email_recipients" long varchar NULL,
    "email_smtp_server_name" long varchar NULL,
    "email_smtp_port" integer NULL,
    "email_smtp_sender_name" long varchar NULL,
    "email_smtp_sender_address" long varchar NULL,
    "email_smtp_auth_user_name" long varchar NULL,
    "email_smtp_auth_password" long varchar NULL,
    "email_user_id" long varchar NULL,
    "email_user_password" long varchar NULL,
    CONSTRAINT "maint_plan_pk" PRIMARY KEY ( "plan_id" ASC )
)
go

CREATE TABLE "dbo"."maint_plan_report" (
	"plan_id" unsigned int NOT NULL,
	"start_time" timestamp NOT NULL,
	"finish_time" timestamp NULL,
	"success" bit NOT NULL,
	"report" long varchar NULL,
	CONSTRAINT "maint_plan_report_pk" PRIMARY KEY ( "plan_id" ASC, "start_time" ASC )
)
go


CREATE TABLE "itcase"."MultiApplianceConfig" (
    "app_id" numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"ip" varchar(16) NOT NULL
   ,"port" varchar(8) NULL
   ,"itcase_version" varchar (32) NULL
   ,"sda_version" varchar (32) NULL
)
go


CREATE TABLE "itcase"."ComplianceInProgress" (
     "policyRuleId"   numeric(19,0) NOT NULL  ,
      "startedAt" "datetime" NOT NULL DEFAULT CURRENT DATE,
      PRIMARY KEY ("policyRuleId")
      )
go 
        
CREATE TABLE "itcase"."LogMessageEntry" (
    "id"            numeric(19,0) NOT NULL DEFAULT autoincrement
   ,"version"       integer NULL
   ,"name"          varchar(255) NOT NULL
   ,"creationTime"  "datetime" NOT NULL
   ,"logLevel"      varchar(32) NOT NULL
   ,"logThread"     varchar(255) NOT NULL
   ,"logPackage"    varchar(255) NULL
   ,"logClass"      varchar(255) NULL
   ,"logNDCUnique"  varchar(255) NULL
   ,"logMessage"    varchar(1024) NOT NULL
   ,"logException"  varchar(1024) NULL
   ,"logCause"      varchar(1024) NULL
   ,PRIMARY KEY ("id")
)
go

-------------------------------------------------
--   Reload column statistics
-------------------------------------------------

if db_property('PageSize') >= 4096 then
    LOAD STATISTICS "itcase"."Role"."id"
	64, -1, 10, 10,
	0x000000000000f03f000000000000f03f00000000000000400000000000000040000000000000084000000000000008400000000000001040000000000000104000000000000014400000000000001440,
	0x00000000cdcc4c3e00000000cdcc4c3e00000000cdcc4c3e00000000cdcc4c3e00000000cdcc4c3e
end if
go

if db_property('PageSize') >= 4096 then
    LOAD STATISTICS "itcase"."Role"."version"
	64, -1, 2, 2,
	0x00000000000000000000000000000000,
	0x000000000000803f
end if
go

if db_property('PageSize') >= 4096 and
   db_property('Collation') = '1252LATIN1' then
    LOAD STATISTICS "itcase"."Role"."description"
	66, -1, 6, 6,
	0x503b7c12d2318daa6a3aa616121a0acfebe6323dacabce6cb00808080808,
	0x00000000cdcc4c3ecdcc4c3ecdcc4c3ecdcc4c3ecdcc4c3e
end if
go

if db_property('PageSize') >= 4096 and
   db_property('Collation') = '1252LATIN1' then
    LOAD STATISTICS "itcase"."Role"."enableStatus"
	66, -1, 2, 2,
	0xb04b73120b736b2fb008,
	0x000000000000803f
end if
go

if db_property('PageSize') >= 4096 and
   db_property('Collation') = '1252LATIN1' then
    LOAD STATISTICS "itcase"."Role"."globalPermission"
	66, -1, 6, 6,
	0xb04b7312d2318daa6a3aa616121a0acfebe6323dacabce6c000808080808,
	0x00000000cdcc4c3ecdcc4c3ecdcc4c3ecdcc4c3ecdcc4c3e
end if
go

if db_property('PageSize') >= 4096 and
   db_property('Collation') = '1252LATIN1' then
    LOAD STATISTICS "itcase"."Role"."objPermission"
	66, -1, 3, 3,
	0xb04b73124c5c67dcfce3edd6000808,
	0x000000009a99193fcdcccc3e
end if
go

if db_property('PageSize') >= 4096 then
    LOAD STATISTICS "itcase"."UserProfile"."id"
	64, -1, 20, 20,
	0x000000000000f03f000000000000f03f00000000000000400000000000000040000000000000084000000000000008400000000000001040000000000000104000000000000014400000000000001440000000000000184000000000000018400000000000001c400000000000001c40000000000000204000000000000020400000000000002240000000000000224000000000000024400000000000002440,
	0x00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d
end if
go

if db_property('PageSize') >= 4096 then
    LOAD STATISTICS "itcase"."UserProfile"."version"
	64, -1, 2, 2,
	0x00000000000000000000000000000000,
	0x000000000000803f
end if
go

if db_property('PageSize') >= 4096 and
   db_property('Collation') = '1252LATIN1' then
    LOAD STATISTICS "itcase"."UserProfile"."authenticationType"
	66, -1, 3, 3,
	0x486b721295bbcdb8b883e1baf80808,
	0x000000000000003f0000003f
end if
go

if db_property('PageSize') >= 4096 and
   db_property('Collation') = '1252LATIN1' then
    LOAD STATISTICS "itcase"."UserProfile"."email"
	66, -1, 2, 2,
	0xe8c14812a4054f95e808,
	0x000000000000803f
end if
go

if db_property('PageSize') >= 4096 and
   db_property('Collation') = '1252LATIN1' then
    LOAD STATISTICS "itcase"."UserProfile"."enableStatus"
	66, -1, 2, 2,
	0xe8c148120b736b2fe808,
	0x000000000000803f
end if
go

if db_property('PageSize') >= 4096 and
   db_property('Collation') = '1252LATIN1' then
    LOAD STATISTICS "itcase"."UserProfile"."firstName"
	66, -1, 11, 11,
	0xe8c1481202d04ddc45d3611b02d046dc59d3611b02d04adc5ed3611b02d04bdc63d3611b02d04cdc68d3611b0008080808080808080808,
	0x00000000cdcccc3dcdcccc3dcdcccc3dcdcccc3dcdcccc3dcdcccc3dcdcccc3dcdcccc3dcdcccc3dcdcccc3d
end if
go

if db_property('PageSize') >= 4096 and
   db_property('Collation') = '1252LATIN1' then
    LOAD STATISTICS "itcase"."UserProfile"."lastName"
	66, -1, 3, 3,
	0xe8c1481295bbcdb8b883e1ba000808,
	0x000000000000003f0000003f
end if
go

if db_property('PageSize') >= 4096 and
   db_property('Collation') = '1252LATIN1' then
    LOAD STATISTICS "itcase"."UserProfile"."nick"
	66, -1, 11, 11,
	0xe8c148121d5468a1bf76ae867cb53b75ce8d2a4a225468a1d38d2a4a275468a1d88d2a4a2c5468a1dd8d2a4a0008080808080808080808,
	0x00000000cdcccc3dcdcccc3dcdcccc3dcdcccc3dcdcccc3dcdcccc3dcdcccc3dcdcccc3dcdcccc3dcdcccc3d
end if
go

if db_property('PageSize') >= 4096 and
   db_property('Collation') = '1252LATIN1' then
    LOAD STATISTICS "itcase"."UserProfile"."objPermission"
	66, -1, 3, 3,
	0xe8c14812fce3edd64c5c67dc000808,
	0x000000006666663fcdcccc3d
end if
go

if db_property('PageSize') >= 4096 and
   db_property('Collation') = '1252LATIN1' then
    LOAD STATISTICS "itcase"."UserProfile"."password"
	66, -1, 2, 2,
	0xe8c148127db458d70008,
	0x000000000000803f
end if
go

if db_property('PageSize') >= 4096 then
    LOAD STATISTICS "itcase"."UserProfile_Role"."UserProfile_id"
	64, -1, 20, 20,
	0x000000000000f03f000000000000f03f00000000000000400000000000000040000000000000084000000000000008400000000000001040000000000000104000000000000014400000000000001440000000000000184000000000000018400000000000001c400000000000001c40000000000000204000000000000020400000000000002240000000000000224000000000000024400000000000002440,
	0x00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d00000000cdcccc3d
end if
go

if db_property('PageSize') >= 4096 then
    LOAD STATISTICS "itcase"."UserProfile_Role"."roles_id"
	64, -1, 22, 6,
	0x0000000000000000000000000000f03f0000000000000040000000000000084000000000000010400000000000001440,
	0x00000000cdcc4c3ecdcc4c3ecdcc4c3ecdcc4c3ecdcc4c3e
end if
go

-------------------------------------------------
--   Reload data - each file is a CSV one where the values are in the order specified in the LOAD TABLE command
-------------------------------------------------

LOAD TABLE "itcase"."Breach" ("id","version","name","creationTime","lastModificationTime","acknowledgedByUserProfileId","acknowledgedTime","classification","closedTime","description","lastCheckedTime","policyRuleId","priority","recommendation","sourceElementId","modifiedByUserId","paramViolationInfo_id","hasChildren","parent_id")
    FROM './unload/Breaches.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."ParamViolationInfo" ("id","version","name","actualValue","desiredStateValue","operator","paramDefId")
    FROM './unload/ParamViolationInfo.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."Breach_elements" ("Breach_id","element")
    FROM './unload/Breach_elements.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."ChangeEvent" ("id","version","name","changeType","creationTime","description","objectName","sourceDm","objectType","userProfileId")
    FROM './unload/ChangeEvents.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."ChangeEvent_Info" ("ChangeEvent_id","info_value","info_name")
    FROM './unload/ChangeEvent_Info.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."Criteria" ("id","version","displayName","dmNameId")
    FROM './unload/Criteria.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."Criteria_description" ("Criteria_id","element")
    FROM './unload/CriteriaDesc.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."Element" ("id","version","creationTime","name","type","dmNameId","sourceDm")
    FROM './unload/Elements.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."ElementProperty" ("id","version","sourceDm","type","name")
    FROM './unload/EProp.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."EmailSettings" ("id","version","host","port","emailPassword","emailUser","systemSenderAddress")
    FROM './unload/EmailSettings.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."LdapSettings" ("id","version","ADServer","baseDn","domainName","port","server")
    FROM './unload/LdapSettings.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."LdapUserDnPattern" ("id","version","pattern","ldapSettings")
    FROM './unload/LdapUserDnPattern.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."Note" ("id","version","name","creationTime","lastModificationTime","text","lastModifiedBy_id","breach","createdBy_id","changeEvent")
    FROM './unload/Notes.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."ParameterDefinition" ("id","version","name","type","minValue","maxValue","parameterDefinition_id","operator","desiredState","desiredStateMouseOver","exampleText")
    FROM './unload/ParamDef.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."ParameterDefinition_allowedValues" ("ParameterDefinition_id","element")
    FROM './unload/ParamAllowed.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."ParameterValue" ("id","version","enableStatus","name","value","policyRule_id")
    FROM './unload/ParamVal.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."Policy" ("id","version","name","creationTime","lastModificationTime","templateType","description","enableStatus","templateId","lastModifiedBy_id","notificationSettings_id","createdBy_id")
    FROM './unload/Policies.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."PolicyGroup" ("id","version","name","creationTime","lastModificationTime","description","createdBy_id","lastModifiedBy_id")
    FROM './unload/PolicyGroups.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."PolicyGroup_Policy" ("PolicyGroup_id","Policy_id")
    FROM './unload/PolicyGroup_Policy.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."PolicyNotificationSettings" ("id","version","SNMPTrapsEnabled","emailEnabled")
    FROM './unload/PolicyNotificationSettings.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."PolicyNotificationSettings_UserProfile" ("PolicyNotificationSettings_id","notifyUsersList_id")
    FROM './unload/PolicyNotifications_UserProfile.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."PolicyNotificationSettings_runScripts" ("PolicyNotificationSettings_id","element")
    FROM './unload/PolicyNotifications_Scripts.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."PolicyRule" ("id","version","name","creationTime","lastModificationTime","templateType","description","enableStatus","lastCheckVersion","templateId","criteria_id","rule_id","policy","createdBy_id","lastModifiedBy_id","classification","priority","ruleClass")
    FROM './unload/PolicyRules.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."Policy_Scope" ("Policy_id","scopes_id")
    FROM './unload/Policy_Scope.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."PolicyRetention" ("id","version","enabled","objectType","retentionTimeUnit","retentionTimeValue")
    FROM './unload/PolicyRetention.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go


LOAD TABLE "itcase"."DbScheduler" ("id","version","day","dbOperation","enabled","hour","minute")
    FROM './unload/DbScheduler.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go


LOAD TABLE "itcase"."Role" ("id","version","name","description","enableStatus","globalPermission","objPermission","authPrivList")
    FROM './unload/Roles.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."Rule" ("id","version","name","breachMessage","ruleClass","description","investigateMessage","priority","recommendation","classification","ruleLogicClass","criteria_id")
    FROM './unload/Rule.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."Rule_ElementProperty" ("Rule_id","elementProperties_id")
    FROM './unload/REProp.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go


LOAD TABLE "itcase"."Scope" ("id","version","description","displayName","dmNameId","type")
    FROM './unload/Scope.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."SnmpTrapDestination" ("id","version","enabled","host","port","community")
    FROM './unload/SnmpTrapDestination.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."UserProfile" ("id","version","authenticationType","email","enableStatus","firstName","lastName","nick","objPermission","password","lastLoginTime","lastLogoutTime")
    FROM './unload/UserProfile.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

LOAD TABLE "itcase"."UserProfile_Role" ("UserProfile_id","roles_id")
    FROM './unload/UserProfile_Role.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

-- NOTE: any changes in columns order may require changing the ConfigGroupDaoImpl#writeGroup method
LOAD TABLE "itcase"."ConfigGroup" ("id","version","name","creationTime","lastModificationTime","modifiedByUserId","parentGroup","description")
    FROM './unload/ConfigGroup.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

-- NOTE: any changes in columns order may require changing the ConfigValueDaoImpl#writeValue method
LOAD TABLE "itcase"."ConfigValue" ("id","version","name","creationTime","lastModificationTime","modifiedByUserId","containerGroup","minValue","maxValue","valueSource","valueType","description","valueString")
    FROM './unload/ConfigValue.dat'
    FORMAT 'ASCII' QUOTES ON
    ORDER OFF ESCAPES ON
    CHECK CONSTRAINTS OFF COMPUTES OFF
    STRIP OFF DELIMITED BY ','
    ENCODING 'windows-1252'
go

commit work
go



-------------------------------------------------
--   Create materialized views
-------------------------------------------------

commit
go

-------------------------------------------------
--   Create indexes
-------------------------------------------------

CREATE INDEX "BreachCreationTimeIndex" ON "itcase"."Breach"
    ( "creationTime" )
go

COMMENT ON INDEX "itcase"."Breach"."BreachCreationTimeIndex" IS
	'Breach creation time index'
go

CREATE INDEX "ChangeEventObjectsIndex" ON "itcase"."ChangeEvent"
    ( "objectType","objectName" )
go

COMMENT ON INDEX "itcase"."ChangeEvent"."ChangeEventObjectsIndex" IS
	'Change event(s) object(s) types and names index'
go

CREATE INDEX "ChangeEventTimeIndex" ON "itcase"."ChangeEvent"
    ( "creationTime" )
go

COMMENT ON INDEX "itcase"."ChangeEvent"."ChangeEventTimeIndex" IS
	'Change event creation time index'
go

CREATE INDEX "ChangeEventTypeIndex" ON "itcase"."ChangeEvent"
    ( "changeType" )
go

COMMENT ON INDEX "itcase"."ChangeEvent"."ChangeEventTypeIndex" IS
	'Change type index'
go

CREATE INDEX "ElementNameIndex" ON "itcase"."Element"
    ( "dmNameId" )
go

COMMENT ON INDEX "itcase"."Element"."ElementNameIndex" IS
	'Element DM name index'
go

CREATE INDEX "ElementCreationTimeIndex" ON "itcase"."Element"
    ( "creationTime" )
go

COMMENT ON INDEX "itcase"."Element"."ElementCreationTimeIndex" IS
	'Element insertion time index'
go

-------------------------------------------------
--   Create foreign keys
-------------------------------------------------

ALTER TABLE "itcase"."Breach"
    ADD FOREIGN KEY "FK7715B2715E939EC6" ("paramViolationInfo_id")
    REFERENCES "itcase"."ParamViolationInfo" ("id")

go

ALTER TABLE "itcase"."Breach"
    ADD FOREIGN KEY "FK7715B2717A91A3ED" ("parent_id")
    REFERENCES "itcase"."Breach" ("id")

go

ALTER TABLE "itcase"."Breach_changeUids"
    ADD FOREIGN KEY "FK65FD58818DA2B0E6" ("Breach_id")
    REFERENCES "itcase"."Breach" ("id")

go

ALTER TABLE "itcase"."Breach_closeChangeUids"
    ADD FOREIGN KEY "FKB890A03D8DA2B0E6" ("Breach_id")
    REFERENCES "itcase"."Breach" ("id")

go

ALTER TABLE "itcase"."PendingBreach"
    ADD FOREIGN KEY "FK2B879EA85E939EC6" ("paramViolationInfo_id")
    REFERENCES "itcase"."ParamViolationInfo" ("id")

go

ALTER TABLE "itcase"."PendingBreach"
    ADD FOREIGN KEY "FK2B879EA8E3BB354C" ("parent_id")
    REFERENCES "itcase"."PendingBreach" ("id")

go

ALTER TABLE "itcase"."PendingBreach_elements"
    ADD FOREIGN KEY "FK854250AEBE57928E" ("PendingBreach_id")
    REFERENCES "itcase"."PendingBreach" ("id")

go

ALTER TABLE "itcase"."PendingBreach_changeUids"
    ADD FOREIGN KEY "FK9C8A73EABE57928E" ("PendingBreach_id")
    REFERENCES "itcase"."PendingBreach" ("id")

go

ALTER TABLE "itcase"."Breach_elements"
    ADD FOREIGN KEY "FK9A836F058DA2B0E6" ("Breach_id")
    REFERENCES "itcase"."Breach" ("id")

go

ALTER TABLE "itcase"."ChangeEvent_Info"
    ADD FOREIGN KEY "FK23C5074391365FBD" ("ChangeEvent_id")
    REFERENCES "itcase"."ChangeEvent" ("id")

go

ALTER TABLE "itcase"."Criteria_description"
    ADD FOREIGN KEY "FKDC09DF3C8C591745" ("Criteria_id")
    REFERENCES "itcase"."Criteria" ("id")

go

ALTER TABLE "itcase"."LdapUserDnPattern"
    ADD FOREIGN KEY "FK52D1AB94743131A3" ("ldapSettings")
    REFERENCES "itcase"."LdapSettings" ("id")

go

ALTER TABLE "itcase"."Note"
    ADD FOREIGN KEY "FK252412333A6817" ("changeEvent")
    REFERENCES "itcase"."ChangeEvent" ("id")

go

ALTER TABLE "itcase"."Note"
    ADD FOREIGN KEY "FK2524122AA869AA" ("createdBy_id")
    REFERENCES "itcase"."UserProfile" ("id")

go

ALTER TABLE "itcase"."Note"
    ADD FOREIGN KEY "FK252412ACDBB96E" ("breach")
    REFERENCES "itcase"."Breach" ("id")

go

ALTER TABLE "itcase"."Note"
    ADD FOREIGN KEY "FK2524126611FB93" ("lastModifiedBy_id")
    REFERENCES "itcase"."UserProfile" ("id")

go

ALTER TABLE "itcase"."ParameterDefinition"
    ADD FOREIGN KEY "FK2803CDBC38F8EB05" ("parameterDefinition_id")
    REFERENCES "itcase"."Rule" ("id")

go

ALTER TABLE "itcase"."ParameterDefinition_allowedValues"
    ADD FOREIGN KEY "FK6E1393A780FF2A39" ("ParameterDefinition_id")
    REFERENCES "itcase"."ParameterDefinition" ("id")

go

ALTER TABLE "itcase"."Policy"
    ADD FOREIGN KEY "FK8ED291522AA869AA" ("createdBy_id")
    REFERENCES "itcase"."UserProfile" ("id")

go

ALTER TABLE "itcase"."Policy"
    ADD FOREIGN KEY "FK8ED29152F5D7EAB7" ("notificationSettings_id")
    REFERENCES "itcase"."PolicyNotificationSettings" ("id")

go

ALTER TABLE "itcase"."Policy"
    ADD FOREIGN KEY "FK8ED291526611FB93" ("lastModifiedBy_id")
    REFERENCES "itcase"."UserProfile" ("id")

go

ALTER TABLE "itcase"."PolicyGroup"
    ADD FOREIGN KEY "FKE32B784D2AA869AA" ("createdBy_id")
    REFERENCES "itcase"."UserProfile" ("id")

go

ALTER TABLE "itcase"."PolicyGroup"
    ADD FOREIGN KEY "FKE32B784D6611FB93" ("lastModifiedBy_id")
    REFERENCES "itcase"."UserProfile" ("id")

go

ALTER TABLE "itcase"."PolicyGroup_Policy"
    ADD FOREIGN KEY "FK427A958451ED228F" ("PolicyGroup_id")
    REFERENCES "itcase"."PolicyGroup" ("id")

go

ALTER TABLE "itcase"."PolicyGroup_Policy"
    ADD FOREIGN KEY "FK427A9584980A0B45" ("Policy_id")
    REFERENCES "itcase"."Policy" ("id")

go


ALTER TABLE "itcase"."Policy"
    ADD FOREIGN KEY "FK8ED29152B7315A8F" ("lastCheckTime_id")
    REFERENCES "itcase"."ComplianceInfo" ("id")

go

ALTER TABLE "itcase"."PolicyNotificationSettings_UserProfile"
    ADD FOREIGN KEY "FKD38EF81F723004AC" ("notifyUsersList_id")
    REFERENCES "itcase"."UserProfile" ("id")

go

ALTER TABLE "itcase"."PolicyNotificationSettings_UserProfile"
    ADD FOREIGN KEY "FKD38EF81F9F2E8D45" ("PolicyNotificationSettings_id")
    REFERENCES "itcase"."PolicyNotificationSettings" ("id")

go

ALTER TABLE "itcase"."PolicyNotificationSettings_additionalEmails"
    ADD FOREIGN KEY "FKB212131D9F2E8D45" ("PolicyNotificationSettings_id")
    REFERENCES "itcase"."PolicyNotificationSettings" ("id")

go

ALTER TABLE "itcase"."PolicyNotificationSettings_runScripts"
    ADD FOREIGN KEY "FKC783BEFC9F2E8D45" ("PolicyNotificationSettings_id")
    REFERENCES "itcase"."PolicyNotificationSettings" ("id")

go

ALTER TABLE "itcase"."PolicyRule"
    ADD FOREIGN KEY "FKD5CCA36E2AA869AA" ("createdBy_id")
    REFERENCES "itcase"."UserProfile" ("id")

go

ALTER TABLE "itcase"."PolicyRule"
    ADD FOREIGN KEY "FKD5CCA36E6AEB492F" ("policy")
    REFERENCES "itcase"."Policy" ("id")

go

ALTER TABLE "itcase"."PolicyRule"
    ADD FOREIGN KEY "FKD5CCA36E8C591745" ("criteria_id")
    REFERENCES "itcase"."Criteria" ("id")

go

ALTER TABLE "itcase"."PolicyRule"
    ADD FOREIGN KEY "FKD5CCA36E6611FB93" ("lastModifiedBy_id")
    REFERENCES "itcase"."UserProfile" ("id")

go

ALTER TABLE "itcase"."PolicyRule"
    ADD FOREIGN KEY "FKD5CCA36E618B75C5" ("rule_id")
    REFERENCES "itcase"."Rule" ("id")

go

ALTER TABLE "itcase"."Policy_Scope"
    ADD FOREIGN KEY "FKA97D7D074BDAD504" ("scopes_id")
    REFERENCES "itcase"."Scope" ("id")

go

ALTER TABLE "itcase"."Policy_Scope"
    ADD FOREIGN KEY "FKA97D7D07980A0B45" ("Policy_id")
    REFERENCES "itcase"."Policy" ("id")

go

ALTER TABLE "itcase"."Rule"
    ADD FOREIGN KEY "FK270B1C8C591745" ("criteria_id")
    REFERENCES "itcase"."Criteria" ("id")

go

ALTER TABLE "itcase"."Rule_ElementProperty"
    ADD FOREIGN KEY "FK35E405AEC63C6ED1" ("elementProperties_id")
    REFERENCES "itcase"."ElementProperty" ("id")

go

ALTER TABLE "itcase"."Rule_ElementProperty"
    ADD FOREIGN KEY "FK35E405AE618B75C5" ("Rule_id")
    REFERENCES "itcase"."Rule" ("id")

go

ALTER TABLE "itcase"."UserProfile_Role"
    ADD FOREIGN KEY "FKE887F0F73AFF3942" ("roles_id")
    REFERENCES "itcase"."Role" ("id")

go

ALTER TABLE "itcase"."UserProfile_Role"
    ADD FOREIGN KEY "FKE887F0F727846B0B" ("UserProfile_id")
    REFERENCES "itcase"."UserProfile" ("id")

go

ALTER TABLE "itcase"."ConfigValue"
    ADD FOREIGN KEY "FKE887F0F707031965" ("containerGroup")
    REFERENCES "itcase"."ConfigGroup" ("id")

go

ALTER TABLE "itcase"."ParameterValue"
    ADD FOREIGN KEY "FK74C9B6824309065" ("policyRule_id")
    REFERENCES "itcase"."PolicyRule" ("id")

go

       
commit work
go


-------------------------------------------------
--   Create functions
-------------------------------------------------

commit
go



-------------------------------------------------
--   Create views
-------------------------------------------------

commit
go


SET TEMPORARY OPTION force_view_creation='ON'
go

SET TEMPORARY OPTION force_view_creation='OFF'
go

call dbo.sa_recompile_views(1)
go


-------------------------------------------------
--   Create user messages
-------------------------------------------------


-------------------------------------------------
--   Create procedures
-------------------------------------------------

commit
go


call dbo.sa_recompile_views(0)
go


-------------------------------------------------
--   Create triggers
-------------------------------------------------

commit
go



-------------------------------------------------
--   Create SQL Remote definitions
-------------------------------------------------


-------------------------------------------------
--   Create MobiLink definitions
-------------------------------------------------


-------------------------------------------------
--   Create logins
-------------------------------------------------


-------------------------------------------------
--   Create events
-------------------------------------------------


-------------------------------------------------
--   Create services
-------------------------------------------------


-------------------------------------------------
--   Set DBA password
-------------------------------------------------

GRANT CONNECT TO DBA IDENTIFIED BY ENCRYPTED '\x01\x53\x7f\x26\xeb\x8f\x8d\x92\x98\x00\xc9\x56\xe3\xb9\x8b\xb9\x50\x67\xf1\xc1\x0e\xc7\xdd\x8e\x3e\xb8\x7c\x8c\x97\x6d\xa7\xe2\xad\xc3\x58\x7a\xe4'
go


-------------------------------------------------
--   Set md_info READONLY and CATALOG to return false
-------------------------------------------------
update "dbo"."spt_mda" set "dbo"."spt_mda"."querytype" = 4, "dbo"."spt_mda"."query" = '0'
where "dbo"."spt_mda"."mdinfo" = 'SET_READONLY_FALSE' ;
update "dbo"."spt_mda" set "dbo"."spt_mda"."querytype" = 4, "dbo"."spt_mda"."query" = '0'
where "dbo"."spt_mda"."mdinfo" = 'SET_READONLY_TRUE' ;
update "dbo"."spt_mda" set "dbo"."spt_mda"."querytype" = 2
where "dbo"."spt_mda"."mdinfo" = 'SET_CATALOG' ;
commit
go


-------------------------------------------------
--   Create options
-------------------------------------------------

SET OPTION date_order =
go

SET OPTION PUBLIC.preserve_source_format =
go

SET OPTION "PUBLIC"."preserve_source_format"='On'
go

SET OPTION "PUBLIC"."max_query_tasks" = 1
go

SET OPTION "PUBLIC"."string_rtruncation" = 'Off'
go