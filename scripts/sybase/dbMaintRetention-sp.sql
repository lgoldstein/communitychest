
CALL sa_make_object( 'procedure','POLICY_RETENTION_manager','itcase' );
go
ALTER PROCEDURE "itcase"."POLICY_RETENTION_manager"(in @backupId integer default 0
        , in @day varchar(120) default ''
        , in @hour int   default HOUR(dateadd(minute ,2,getdate())) 
        , in @minute int default MINUTE(dateadd(minute ,2,getdate())) 
        , in @enabled tinyint default 1
        , in @startDate date default DATE(dateadd(minute ,2,getdate())) ) 

/* RESULT( column_name column_type, ... ) */

begin
declare @sql varchar(4096);
declare @sqlenabled varchar(50);
declare @sqlday varchar(120);
declare @eventName varchar(120);
declare @BACKUP_PATH varchar(255) ;
	
  set @eventName = 'POLICY_RETENTION' ;
  
  -------- Initialize parameters (null parameters are not initialized by the "default" initialization...)
	 set @backupId = isnull(@backupId ,  0 ) ;
	 set @day = isnull(@day ,  '' ) ;
	 set @hour = isnull(@hour ,  HOUR(dateadd(minute ,2,getdate())) ) ;
	 set @minute = isnull(@minute , MINUTE(dateadd(minute ,2,getdate())) ) ;
	 set @enabled = isnull(@enabled ,  1 ) ;
	 set @startDate = isnull(@startDate ,  DATE(dateadd(minute ,2,getdate())) ) ;
	 
	 set @BACKUP_PATH = replace ( db_property('File') , db_name()+'.db' , 'backup') ;

if( @day = '') then -- run once
        set @sqlday = ' '  ;
    else 
        set @sqlday = string (' EVERY 24 HOURS  ON ( ''',@day,''' ) ' ) ;
  end if;
---------------------------------------- run later
  if( @backupId = 0) then 
        insert itcase.DbBackupOnce (hour  , minute  , startDate) values (@hour  , @minute  , @startDate);
        set @backupId = @@identity ;
        set @eventName = 'POLICY_RETENTIONOnce' ;
        update itcase.DbBackupOnce set eventName = string(@eventName,@backupId)  where "id" = @backupId  ;
        commit ;
  end if ;
--------------------------------------- run now
 if( @backupId = -1) then 
       insert itcase.DbBackupOnce (hour  , minute  , startDate) values (@hour  , @minute  , @startDate);
        set @backupId = @@identity ;
        set @eventName = 'POLICY_RETENTIONOnceNow' ;
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
            	, ' HANDLER\x0D\x0A    begin\x0D\x0A    call itcase.do_retention (''',@eventName,@backupId,''') ;\x0D\x0A end\x0D\x0A') ;

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
            	, ' HANDLER\x0D\x0A    begin\x0D\x0A    call itcase.do_retention (''',@eventName,@backupId,''') ;\x0D\x0A end\x0D\x0A') ;
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
-------------------------------------------------------------
------------------ do_retention ----------------------------
-------------------------------------------------------------


CALL sa_make_object( 'procedure','do_retention','itcase' );

ALTER PROCEDURE "itcase"."do_retention"(in @backup_name varchar(128))


begin
  declare @BACKUP_PATH varchar(255) ;

  declare @nextLabel varchar(255) ;
  
  declare @DateValue varchar(255) ;
	
  declare @SUCCESS bit;

  declare @START_TIME timestamp;

  declare @PLAN_ID unsigned integer;

  declare @REPORT long varchar;

  declare @MSG long varchar;

  declare @ERROR_MSG long varchar;

  declare @ERROR_STATE long varchar;

  declare @ERROR_CODE integer;

	 declare @rows int ; 
 
  set @rows = 0 ;
  
  set @SUCCESS=1;

  set @START_TIME=current timestamp;

  set @PLAN_ID=(select plan_id from dbo.maint_plan where plan_name = @backup_name);

  set @REPORT=null;

  // Print beginning of report message

  set @MSG='Maintenance plan POLICY_RETENTION for itcase on itcase started on ' || current date || ' at ' || current time;

  set @REPORT=@REPORT || @MSG || '\x0A';

 

    // POLICY_RETENTION

    set @MSG='POLICY_RETENTION started on ' || current date || ' at ' || current time;

    set @REPORT=@REPORT || @MSG || '\x0A';

    set @MSG='Deleting from table BREACH started on ' || current date || ' at ' || current time ; 

    set @REPORT=@REPORT || @MSG || '\x0A';
		
---   retention BREACH
		
		begin 
		 	
    call  itcase.do_retention_breach ( @rows ) ;
    
   
		
    set @MSG=string (@rows,' rows deleted from BREACH. ','Deleting from table BREACH finished on ' || current date || ' at ' || current time );

    set @REPORT=@REPORT || @MSG || '\x0A' ;
  exception
    when others then
    
      select ERRORMSG(*),sqlstate,sqlcode into @ERROR_MSG,@ERROR_STATE,@ERROR_CODE;
      set @SUCCESS=0;
      set @MSG='The maintenance plan has ended because of the following error:\x0A'
         || @ERROR_MSG || '\x0ASQLSTATE: ' || @ERROR_STATE || '\x0ASQLCODE:  ' || @ERROR_CODE;
      set @REPORT=@REPORT || @MSG || '\x0A' ;
      


      
  end;

--ChangeEvent_AUDITCHANGE:
--  delete from ChangeEvent where sourceDm is null
	begin
     call  itcase.do_retention_ChangeEvent_AUDITCHANGE (  ) ;
     
    set @MSG=string (@@rowcount,' rows deleted from ChangeEvent (AUDITCHANGE). ','Deleting from table BREACH finished on ' || current date || ' at ' || current time );

    set @REPORT=@REPORT || @MSG || '\x0A' ;
  exception
    when others then
      select ERRORMSG(*),sqlstate,sqlcode into @ERROR_MSG,@ERROR_STATE,@ERROR_CODE;
      set @SUCCESS=0;
      set @MSG='The maintenance plan has ended because of the following error:\x0A'
         || @ERROR_MSG || '\x0ASQLSTATE: ' || @ERROR_STATE || '\x0ASQLCODE:  ' || @ERROR_CODE;
      set @REPORT=@REPORT || @MSG || '\x0A'
  end;

--:ChangeEvent_ELEMENTCHANGE
--  delete from ChangeEvent where sourceDm is not null
	begin
	
    call  "itcase"."do_retention_ChangeEvent_ELEMENTCHANGE" (  ) ;

    set @MSG=string (@@rowcount,' rows deleted from ChangeEvent(ELEMENTCHANGE). ','Deleting from table BREACH finished on ' || current date || ' at ' || current time );

    set @REPORT=@REPORT || @MSG || '\x0A' ;

  exception
    when others then
      select ERRORMSG(*),sqlstate,sqlcode into @ERROR_MSG,@ERROR_STATE,@ERROR_CODE;
      set @SUCCESS=0;
      set @MSG='The maintenance plan has ended because of the following error:\x0A'
         || @ERROR_MSG || '\x0ASQLSTATE: ' || @ERROR_STATE || '\x0ASQLCODE:  ' || @ERROR_CODE;
      set @REPORT=@REPORT || @MSG || '\x0A'
  end;



  // Print end of report message

  set @MSG='Maintenance plan POLICY_RETENTION for itcase on itcase finished on ' || current date || ' at ' || current time;

  set @REPORT=@REPORT || @MSG || '\x0A';

  // Save report

  insert into dbo.maint_plan_report( plan_id,start_time,finish_time,success,report)

    values( @PLAN_ID,@START_TIME,current timestamp,@SUCCESS,@REPORT) ;

  // Keep only most recent reports

  delete from dbo.maint_plan_report

    where plan_id = @PLAN_ID and not start_time

     = any(select top 5 start_time from dbo.maint_plan_report

      where plan_id = @PLAN_ID order by start_time desc) ;
      
   commit;   

end ;

-------------------------------------------------------------
------------------ unitTran ----------------------------
-------------------------------------------------------------

CALL sa_make_object( 'function','unitTran','itcase' );

ALTER FUNCTION itcase.unitTran( in @timeUnit varchar(55) default null )
returns varchar(55)
on exception resume
begin
declare @retUnit varchar(55) ;

select
		CASE  @timeUnit
			WHEN  'DAY' then 'dd'
			WHEN  'WEEK' then 'wk'
			WHEN  'MONTH' then 'mm'
			WHEN  'YEAR' then 'yy'
			ELSE   ''
		END
			into @retUnit ;
			
	return 	@retUnit ;	
end ;	


-------------------------------------------------------------
------------------ do_retention_breach ----------------------------
-------------------------------------------------------------


CALL sa_make_object( 'procedure','do_retention_breach','itcase' );

ALTER PROCEDURE "itcase"."do_retention_breach"(out @rows int )


begin
  declare @sqlDate varchar(255) ;
  
  declare @DateValue varchar(255) ;
  
	  set @rows = 0 ;	
		select    string ('set @DateValue = ', ' dateadd(', itcase.unitTran(retentionTimeUnit), ', -'  , retentionTimeValue , ',getdate() )' ) 
							into @sqlDate
			from itcase.PolicyRetention WHERE objectType = 'BREACH' and enabled = 1 ;
    if(@@rowcount = 0) then return end if ; 
 		execute immediate with result set off @sqlDate ; 


begin atomic   --   it's like begin transaction 
		
--		delete from Breach_changeUids accroding to foreign key ;
		delete itcase.Breach_changeUids from itcase.BREACH where BREACH.creationTime <= @DateValue and Breach_changeUids.Breach_id = Breach.id ;

--		delete from Breach_closeChangeUids accroding to foreign key ;
		delete itcase.Breach_closeChangeUids from itcase.BREACH where BREACH.creationTime <= @DateValue and Breach_closeChangeUids.Breach_id = Breach.id ;

--		delete from Note according to foreign key  ;
    delete itcase.Note from itcase.BREACH where BREACH.creationTime <= @DateValue and  Note.breach = Breach.id ;


    
--    delete from Breach_elements according to foreign key

    delete itcase.Breach_elements from itcase.BREACH where BREACH.creationTime <= @DateValue and  Breach_elements.Breach_id = Breach.id ; 
--     delete from Breach 
 	  delete itcase.BREACH where creationTime <= @DateValue ;
		
		set @rows = @@rowcount ;

--    delete from Element according to Breach_elements and Breach 

     delete itcase.Element where not exists ( select 1 from itcase.Breach_elements b where Element.id = b.element  )
     									and not exists ( select 1 from itcase.Breach b where Element.id = b.sourceElementId  )
     									    				
end ; -- atomic
    
end ;
-------------------------------------------------------------
------------------ do_retention_ChangeEvent_AUDITCHANGE ----------------------------
-------------------------------------------------------------
	
	
	CALL sa_make_object( 'procedure','do_retention_ChangeEvent_AUDITCHANGE','itcase' );
	
	ALTER PROCEDURE "itcase"."do_retention_ChangeEvent_AUDITCHANGE"(out @rows int )
	
	
	begin
	  declare @sqlDate varchar(255) ;
	  
	  declare @DateValue varchar(255) ;
	  
		  set @rows = 0 ;
			select    string ('set @DateValue = ', ' dateadd(', itcase.unitTran(retentionTimeUnit), ', -'  , retentionTimeValue , ',getdate() )' ) 
								into @sqlDate
				from itcase.PolicyRetention WHERE objectType = 'AUDITCHANGE' and enabled = 1;
	    if(@@rowcount = 0) then return end if ;
	 		execute immediate with result set off @sqlDate ; 
	
	
	begin atomic  --   it's like begin transaction 
			
	--		delete from Note according to foreign key  ;
	    delete itcase.Note from itcase.ChangeEvent 
	    	 where ChangeEvent.sourceDm is null and ChangeEvent.creationTime <= @DateValue 
	    		 and  Note.changeEvent = ChangeEvent."id" ;
	
	--    delete from ChangeEvent_Info according to foreign key
	
	    delete itcase.ChangeEvent_Info  from itcase.ChangeEvent 
	    	 where ChangeEvent.sourceDm is null and ChangeEvent.creationTime <= @DateValue 
	    		 and  ChangeEvent_Info.ChangeEvent_id = ChangeEvent."id" ;
	--     delete from ChangeEvent where sourceDm is null
	 	  delete itcase.ChangeEvent where sourceDm is null and creationTime <= @DateValue ;
			
			set @rows = @@rowcount ;
	
	
	     									    				
	end ;  -- atomic
	    
	end ;
-------------------------------------------------------------
------------------ do_retention_ChangeEvent_ELEMENTCHANGE ----------------------------
-------------------------------------------------------------
	
	
	CALL sa_make_object( 'procedure','do_retention_ChangeEvent_ELEMENTCHANGE','itcase' );
	
	ALTER PROCEDURE "itcase"."do_retention_ChangeEvent_ELEMENTCHANGE"(out @rows int )
	
	
	begin
	  declare @sqlDate varchar(255) ;
	  
	  declare @DateValue varchar(255) ;
	  
		  set @rows = 0 ;
			select    string ('set @DateValue = ', ' dateadd(', itcase.unitTran(retentionTimeUnit), ', -'  , retentionTimeValue , ',getdate() )' ) 
								into @sqlDate
				from itcase.PolicyRetention WHERE objectType = 'ELEMENTCHANGE' and enabled = 1;
	    if(@@rowcount = 0) then return end if ;
	 		execute immediate with result set off @sqlDate ; 
	
	
	begin atomic  --   it's like begin transaction 
			
	--		delete from Note according to foreign key  ;
	    delete itcase.Note from itcase.ChangeEvent 
	    	 where ChangeEvent.sourceDm is not null and ChangeEvent.creationTime <= @DateValue 
	    		 and  Note.changeEvent = ChangeEvent."id" ;
	
	--    delete from ChangeEvent_Info according to foreign key
	
	    delete itcase.ChangeEvent_Info  from itcase.ChangeEvent 
	    	 where ChangeEvent.sourceDm is not null and ChangeEvent.creationTime <= @DateValue 
	    		 and  ChangeEvent_Info.ChangeEvent_id = ChangeEvent."id" ;
	--     delete from ChangeEvent where sourceDm is not null
	 	  delete itcase.ChangeEvent where sourceDm is not null and creationTime <= @DateValue ;
			
			set @rows = @@rowcount ;
	
	
	     									    				
	end ;
	    
	end ;

-------------------------------------------------------
--   admin_reorg_tables -------------------------------
-------------------------------------------------------
CALL sa_make_object( 'procedure','admin_reorg_tables','dba' );

alter proc dba.admin_reorg_tables()
begin
declare @sqlstr  varchar(4000);
 FOR ix AS ixc
  CURSOR FOR
select tables.table_name as @tname
        ,SYSIDX.index_id as @ind_id
        ,SYSIDX.index_name as @ind_name
from SYSIDX , 
(
select top 10 table_id,table_name from SYSTABLE 
where table_page_count > 100
order by count desc ) as tables
where SYSIDX.table_id = tables.table_id
DO
if(@ind_id = 0)then 
    set @sqlstr = 'REORGANIZE TABLE ' + @tname ;
else 
    set @sqlstr = 'REORGANIZE TABLE ' + @tname + ' index ' + @ind_name ;
end if;
message @sqlstr to client;
execute immediate @sqlstr ;
END FOR;
end;

CREATE EVENT "reorg_event"
SCHEDULE "reorg1_schedule" START TIME '23:15' EVERY 24 HOURS START DATE '2008-01-01'
HANDLER
BEGIN 
	call dba.admin_reorg_tables(); 
END ;
-------------------------------------------------------------
------------------  ----------------------------
-------------------------------------------------------------