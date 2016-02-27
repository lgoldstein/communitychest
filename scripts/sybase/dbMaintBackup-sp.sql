
CALL sa_make_object( 'procedure','dbbackup_manager','itcase' );
go
ALTER PROCEDURE "itcase"."dbbackup_manager"(in @backupId integer default null
        ,in  @day varchar(120) default null
        ,in  @hour int   default null 
        ,in @minute int default null 
        ,in @enabled tinyint default null
        ,in @startDate date default null
        )

begin
	
declare @sql varchar(4096);
declare @sqlenabled varchar(50);
declare @sqlday varchar(120);
declare @eventName varchar(120);
declare @BACKUP_PATH varchar(255) ;

  

	-------- Initialize parameters (null parameters are not initialized by the "default" initialization...)
	 set @backupId = isnull(@backupId ,  0 ) ;
	 set @day = isnull(@day ,  '' ) ;
	 set @hour = isnull(@hour ,  HOUR(dateadd(minute ,2,getdate())) ) ;
	 set @minute = isnull(@minute , MINUTE(dateadd(minute ,2,getdate())) ) ;
	 set @enabled = isnull(@enabled ,  1 ) ;
	 set @startDate = isnull(@startDate ,  DATE(dateadd(minute ,2,getdate())) ) ;
	
	 set @BACKUP_PATH = replace ( db_property('File') , db_name()+'.db' , 'backup') ;
	

  set @eventName = 'dbbackup' ;
  if( @day = '') then -- run once
        set @sqlday = ' '  ;
    else 
        set @sqlday = string (' EVERY 24 HOURS  ON ( ''',@day,''' ) ' ) ;
  end if;
---------------------------------------- run later
  if( @backupId = 0) then 
        insert itcase.DbBackupOnce (hour  , minute  , startDate) values (@hour  , @minute  , @startDate);
        set @backupId = @@identity ;
        set @eventName = 'dbbackupOnce' ;
        update itcase.DbBackupOnce set eventName = string(@eventName,@backupId)  where "id" = @backupId  ;
        commit ;
  end if ;
--------------------------------------- run now
 if( @backupId = -1) then 
       insert itcase.DbBackupOnce (hour  , minute  , startDate) values (@hour  , @minute  , @startDate);
        set @backupId = @@identity ;
        set @eventName = 'dbbackupOnceNow' ;
        update itcase.DbBackupOnce set eventName = string(@eventName,@backupId)  where "id" = @backupId  ;
        commit ;
  end if ;
  if( @enabled = 0 ) then
        set @sqlenabled = ' DISABLE ';
    else 
        set @sqlenabled = ' ENABLE ';
  end if;
  if(exists(select plan_name from dbo.maint_plan

      where plan_name = string(@eventName,@backupId))) then

    //   update dbo.maint_plan  , alter event , update parameters

    set @sql = string(' \x0D\x0A   alter EVENT "',@eventName, @backupId 
            	, '_event"\x0D\x0A '
            	, ' SCHEDULE "',@eventName,@backupId,'_schedule" START TIME ''',@hour,':',@minute , ''''
                ,  @sqlday
            	, ' START DATE ''', @startDate ,'''\x0D\x0A '
                , @sqlenabled
            	, ' HANDLER\x0D\x0A    begin\x0D\x0A    call itcase.do_backup (''',@eventName,@backupId,''') ;\x0D\x0A end\x0D\x0A') ;

    message @sql to client;

    execute immediate with result set off @sql ;

  else

    // insert  dbo.maint_plan  , create event , insert parameters
    set @sql = string('  \x0D\x0A create EVENT "',@eventName, @backupId 
            	, '_event"\x0D\x0A '
            	, ' SCHEDULE "',@eventName,@backupId,'_schedule" START TIME ''',@hour,':',@minute , ''''
                ,  @sqlday
            	, ' START DATE ''', @startDate ,'''\x0D\x0A '
                , @sqlenabled
            	, ' HANDLER\x0D\x0A    begin\x0D\x0A    call itcase.do_backup (''',@eventName,@backupId,''') ;\x0D\x0A end\x0D\x0A') ;
   message @sql to client;

    execute immediate with result set off @sql ;

    insert into dbo.maint_plan( plan_name,event_name,disable_new_connections,disconnect_all_users,do_validate,validate_database_check,

      validate_checksum_check,validate_express_check,validate_normal_check,do_backup,disk_backup,

      full_backup,archive_backup,backup_path,tape_backup_prompt,tape_backup_comment,save_report_count,

      report_to_console,email_success,email_failure,email_recipients,email_smtp_server_name,email_smtp_port,

      email_smtp_sender_name,email_smtp_sender_address,email_smtp_auth_user_name,email_smtp_auth_password,

      email_user_id,

      email_user_password)

      select string(@eventName,@backupId),string(@eventName,@backupId,'_event'),0,0,0,

        0,0,0,0,

        1,1,1,0,@BACKUP_PATH,0,'',3,0,0,0,'',null,'',null,null,null,null,null,null

  end if

end;


CALL sa_make_object( 'procedure','do_backup','itcase' );
go

ALTER PROCEDURE "itcase"."do_backup"(in @backup_name varchar(128))

// Note: This is the generated event handler for maintenance plan 'dbbackup'

// Do not modify

begin
  declare @BACKUP_PATH varchar(255) ;

  declare @SUCCESS bit;

  declare @START_TIME timestamp;

  declare @PLAN_ID unsigned integer;

  declare @REPORT long varchar;

  declare @MSG long varchar;

  declare @ERROR_MSG long varchar;

  declare @ERROR_STATE long varchar;

  declare @CST_CONFIG_FOLDER long varchar;
  declare @CLB_FILE long varchar;
  declare @LOCAL_DIRECTORY_FILE long varchar;


  declare @ERROR_CODE integer;

  set @START_TIME=current timestamp;

  set @BACKUP_PATH = replace ( db_property('File') , db_name()+'.db' , 'backup-'|| + convert(char(10),getdate(),105))  ; 
  
  // TODO: read value from environment variable
  set @CST_CONFIG_FOLDER = '/opt/CST/xml/';
  set @CLB_FILE = 'csp.clb';
  set @LOCAL_DIRECTORY_FILE = 'LocalDirectoryData.xml';

  call xp_cmdshell ('mkdir '+ @BACKUP_PATH ) ;

  set @SUCCESS=1;

  set @PLAN_ID=(select plan_id from dbo.maint_plan where plan_name = @backup_name);

  set @REPORT=null;

  // Print beginning of report message

  set @MSG='Maintenance plan dbbackup for itcase on itcase started on ' || current date || ' at ' || current time;

  set @REPORT=@REPORT || @MSG || '\x0A';

  begin

    // Backup

    set @MSG='Backup started on ' || current date || ' at ' || current time;

    set @REPORT=@REPORT || @MSG || '\x0A';

    set @MSG='Backing up to image: '''+@BACKUP_PATH+'''';

    set @REPORT=@REPORT || @MSG || '\x0A';

    backup database directory @BACKUP_PATH ;
    
		BACKUP DATABASE DIRECTORY ''
		TRANSACTION LOG ONLY
		TRANSACTION LOG TRUNCATE;


	// Backup CST files
	call xp_cmdshell ('cp ' + @CST_CONFIG_FOLDER + '/' + @LOCAL_DIRECTORY_FILE + ' ' + @BACKUP_PATH);
	call xp_cmdshell ('cp ' + @CST_CONFIG_FOLDER + '/' + @CLB_FILE + ' ' + @BACKUP_PATH);

    set @MSG='Copying CST files into ' || @BACKUP_PATH  ;

    set @REPORT=@REPORT || @MSG || '\x0A';

    set @MSG='Backup finished on ' || current date || ' at ' || current time;

    set @REPORT=@REPORT || @MSG || '\x0A';

  exception

    when others then

      select ERRORMSG(*),sqlstate,sqlcode into @ERROR_MSG,@ERROR_STATE,@ERROR_CODE;

      set @SUCCESS=0;

      set @MSG='The maintenance plan has ended because of the following error:\x0A'

         || @ERROR_MSG || '\x0ASQLSTATE: ' || @ERROR_STATE || '\x0ASQLCODE:  ' || @ERROR_CODE;

      set @REPORT=@REPORT || @MSG || '\x0A'

  end;

  // Print end of report message

  set @MSG='Maintenance plan dbbackup for itcase on itcase finished on ' || current date || ' at ' || current time;

  set @REPORT=@REPORT || @MSG || '\x0A';

  // Save report

  insert into dbo.maint_plan_report( plan_id,start_time,finish_time,success,report)

    values( @PLAN_ID,@START_TIME,current timestamp,@SUCCESS,@REPORT) ;

  // Keep only most recent reports

  delete from dbo.maint_plan_report

    where plan_id = @PLAN_ID and not start_time

     = any(select top 5 start_time from dbo.maint_plan_report

      where plan_id = @PLAN_ID order by start_time desc)

end ;


CALL sa_make_object( 'procedure','dbscheduler_proc','itcase' );
go

alter proc itcase.dbscheduler_proc ()


BEGIN
    FOR names AS curs CURSOR FOR
    SELECT "id", "day" , "hour" , "minute" , "enabled" FROM itcase.DbScheduler where dbOperation = 'BACKUP' 
    FOR READ ONLY    
    DO   
        MESSAGE string ("id", ':' ,"day" ,':' , "hour" ,':' , "minute" ,':' , "enabled") to client ;
        call "itcase"."dbbackup_manager" ("id", "day" , "hour" , "minute" ,  "enabled" );

    END FOR;
END ;
---------------------------------------------------------------------- clean events
CALL sa_make_object( 'procedure','dbscheduler_clean','itcase' );
go
-- call itcase.dbscheduler_clean ()
alter proc itcase.dbscheduler_clean ()
BEGIN
declare @sql varchar(4096);
    FOR names AS curs CURSOR FOR
    select b.eventName   from itcase.DbBackupOnce b
            where b.startDate <=DATE(getdate()) and eventName is not null  
    FOR READ ONLY
    DO   
        MESSAGE string ("eventName", ':'  ) to client ;
    
        delete dbo.maint_plan where plan_name = "eventName" ;
 
       // delete dbo.maint_plan_report where plan_id = "plan_id" ; foreign key
    set @sql = string ('drop event ', "eventName" , '_event ');
    message @sql to client;

    execute immediate with result set off @sql ;

       --delete itcase.DbBackupOnce where eventName = "eventName" ; 
    commit ;
    END FOR;
    delete itcase.DbBackupOnce 
            where startDate <=DATE(getdate()) and eventName is not null ;
    commit;
END;