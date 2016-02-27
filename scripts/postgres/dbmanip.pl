#!/usr/bin/perl

use File::Spec;
use File::Basename;
use File::Path;
use Cwd;

##################################### Globals ################################### 

$OPT_PGHOME="--pghome";
	$DEFAULT_PGHOME=getEnvVarValue("PGHOME");
$OPT_PGHOST="--host";
	$DEFAULT_HOST_VALUE="localhost";
	$DEFAULT_PGHOST=getEnvVarValue("PGHOST", $DEFAULT_HOST_VALUE);
$OPT_PGPORT="--port";
	$DEFAULT_PORT_VALUE="5432";
	$DEFAULT_PGPORT=getEnvVarValue("PGPORT", $DEFAULT_PORT_VALUE);
$OPT_PGDB="--dbname";
	$DEFAULT_DB_VALUE="postgres";
	$DRWHO_DB_NAME="drwho";
	$DEFAULT_PGDB=getEnvVarValue("PGDATABASE", $DRWHO_DB_NAME);
$OPT_PGUSER="--username";
	$DEFAULT_USER_VALUE="postgres";
	$DEFAULT_PGUSER=getEnvVarValue("PGUSER", $DEFAULT_USER_VALUE);
$OPT_PGBIN="--pgbin";

$OPT_MODE="--mode";
	$MODE_FORMAT="format";
	$MODE_CREATE="create";
	$MODE_MODEL="model";
	$MODE_DATA="data";
	$MODE_UPDATE="update";
	$MODE_DELETE="delete";
	$MODE_DUMP="dump";
	$MODE_RESTORE="restore";
		$DUMP_DEFAULT_SUFFIX=".sql";
		$DUMP_DEFAULT_FOLDER="dump";

$OPT_LOCATION="--location";
	$DEFAULT_LOCATION=Cwd::realpath(getcwd());
$OPT_DUMP_FOLDER="--dump";
	$DEFAULT_DUMP_FOLDER=File::Spec->catfile($DEFAULT_LOCATION, $DUMP_DEFAULT_FOLDER);
$OPT_NAME="--name";
	$DEFAULT_NAME=createDefaultDumpFileName();
$OPT_HELP="--help";

##################################### Functions ################################### 

# if it has any arguments then it returns the 1st one - otherwise returns $^O (i.e., default)
sub resolveOperatingSystem {
	my(@paramsList)=@_;
	if ($#paramsList < 0)
	{
		return $^O;
	}
	else
	{
		return shift(@paramsList);
	}
}

# Checks if the given argument (if any) describes a Windows shell
# SEE resolveOperatingSystem
sub isWindowsShell {
	my $osDescription=resolveOperatingSystem(@_);
	if ($osDescription =~ m/.*Win32.*/)
	{
		return 1;
	}
	else
	{
		return 0;
	}
}

sub getEnvVarValue {
	my($envVarName,$defaultValue)=@_;
	if (exists($ENV{$envVarName}))
	{
		return $ENV{$envVarName};
	}
	else
	{
		return $defaultValue;
	}
}

sub createDefaultDumpFileName {
	my($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst)=localtime(time);
	return sprintf("%04d-%02d-%02d-%02d%02d%02d", ($year + 1900), ($mon + 1), $mday, $hour, $min, $sec).$DUMP_DEFAULT_SUFFIX;
}

# ---------------------------------------------------------------------------- 

sub showUsage {
	print "Usage: dbmanip $MODE_FORMAT/$MODE_CREATE/$MODE_MODEL/$MODE_DATA/$MODE_UPDATE/$MODE_DELETE/$MODE_DUMP/$MODE_RESTORE [OPTIONS]\n";
	print "\n";
	print "Where OPTIONS are: \n";
	print "\n";
		print "\t$OPT_PGHOME - location where PostgreSQL installed - default=$DEFAULT_PGHOME\n";
		print "\t$OPT_PGHOST - host where database resides - default=$DEFAULT_PGHOST\n";
		print "\t$OPT_PGPORT - port at wich service is listening - default=$DEFAULT_PGPORT\n";
		print "\t$OPT_PGDB - database instance to be used - default=$DRWHO_DB_NAME\n";
		print "\t$OPT_PGUSER - login username - default=$DEFAULT_PGUSER\n";
		print "\t$OPT_LOCATION - root folder where SQL scripts/backups/data files reside - default=$DEFAULT_NAME\n";
		print "\t$OPT_DUMP_FOLDER - location where dump file(s) reside - default=$DEFAULT_DUMP_FOLDER\n";
		print "\t$OPT_NAME - name for dump/restore file - default=$DEFAULT_NAME\n";
		print "\t\tNOTE: Must be specified for $MODE_RESTORE\n";
		print "\t$OPT_HELP - show this help message\n";
}

# ---------------------------------------------------------------------------- 

sub dieWithMessage {
	my($code,$msg)=@_;
	print STDERR "ERROR $code: $msg\n\n";
	exit $code;
}

sub dieWithUsage {
	showUsage();
	dieWithMessage(@_);
}
# ---------------------------------------------------------------------------- 

# Parameters: option name, alternative name, value, options hash
# If the option has been mapped by the specified name or the alternative
# one (if defined) then dies with error message. Otherwise maps the
# value to the name and its alternative (if defined)
sub updateOptionValue {
	my($optName,$optVal,$usedOptionsRef)=@_;

	# check if option re-specified
	if (exists($usedOptionsRef->{$optName}))
	{
		dieWithUsage(901, "Option $optName re-specified");
	}

	# mark the option as used
	$usedOptionsRef->{$optName} = $optVal;
	return $optVal;
}

# ---------------------------------------------------------------------------- 

# Parameters: option name, value, options hash
# If specified option already mapped does nothing. Otherwise maps the value
sub updateDefaultOption {
	my($optName,$optVal,$usedOptionsRef)=@_;
	if (!exists($usedOptionsRef->{$optName}))
	{
		if (!defined($optVal))
		{
			dieWithUsage(100, "No default value availalable for $optName");
		}

		$usedOptionsRef->{$optName} = $optVal;	# mark the option as used
	}

	return $usedOptionsRef;
}

# ---------------------------------------------------------------------------- 

sub parseCommandLineOptions {
	my ($usedOptionsRef,$cmdArgsRef)=@_;
	my @cmdArgs=@{ $cmdArgsRef };

	if ($#cmdArgs < 0)
	{
		push(@cmdArgs, $OPT_HELP);
	}

	my	$mode=shift(@cmdArgs);
	updateOptionValue($OPT_MODE, $mode, $usedOptionsRef);

	while (@cmdArgs)
	{
		my	$optSpec=shift(@cmdArgs);
		# if found help request then process no more and die after the usage message
		if ($optSpec eq $OPT_HELP)
		{
			showUsage();
			die;
		}

		my	$argIndex=index($optSpec, '=');
		if (($argIndex < 0) && (argIndex == (length($optSpect) - 1)))
		{
			dieWithMessage(333, "No argument specified for ".$optSpec);
		}

		my	$optName=substr($optSpec, 0, $argIndex);
		my	$optVal=substr($optSpec, $argIndex + 1);
		if (($optName eq $OPT_PGHOME)
		 || ($optName eq $OPT_PGHOST)
		 || ($optName eq $OPT_PGPORT)
		 || ($optName eq $OPT_PGDB)
		 || ($optName eq $OPT_PGUSER)
		 || ($optName eq $OPT_DUMP_FOLDER)
		 || ($optName eq $OPT_NAME)
		 || ($optName eq $OPT_LOCATION))
		{
			updateOptionValue($optName, $optVal, $usedOptionsRef);
		}
		else
		{
			dieWithUsage(911, "Unknown argument: $optSpec");
		}
	}

	if (!exists($usedOptionsRef->{$OPT_MODE}))
	{
		dieWithUsage(1, "No work mode specified");
	}

	updateDefaultOption($OPT_PGHOME, $DEFAULT_PGHOME, $usedOptionsRef);
	my $pgHome=$usedOptionsRef->{$OPT_PGHOME};
	my $pgBinFolder=File::Spec->catfile($pgHome, "bin");
	if (-e $pgBinFolder)
	{
		$usedOptionsRef->{$OPT_PGBIN} = $pgBinFolder;
	}
	else
	{
		dieWithMessage(2, "No bin folder found in specified PG_HOME location: $pgHome");
	}
	
	updateDefaultOption($OPT_PGDB, $DRWHO_DB_NAME, $usedOptionsRef);
	updateDefaultOption($OPT_PGHOST, $DEFAULT_PGHOST, $usedOptionsRef);
	updateDefaultOption($OPT_PGPORT, $DEFAULT_PGPORT, $usedOptionsRef);
	updateDefaultOption($OPT_PGUSER, $DEFAULT_PGUSER, $usedOptionsRef);
	updateDefaultOption($OPT_LOCATION, $DEFAULT_LOCATION, $usedOptionsRef);
	updateDefaultOption($OPT_DUMP_FOLDER,
						File::Spec->catfile($usedOptionsRef->{ $OPT_LOCATION }, $DUMP_DEFAULT_FOLDER),
						$usedOptionsRef);
	updateDefaultOption($OPT_NAME, $DEFAULT_NAME, $usedOptionsRef);

	return $usedOptionsRef;
}

# ---------------------------------------------------------------------------- 

sub getCommandName {
	my($cmd)=@_;
	if (isWindowsShell())
	{
		return $cmd.".exe";
	}
	else
	{
		return $cmd;
	}
}

# ---------------------------------------------------------------------------- 

sub adjustPath {
	my($path)=@_;

	# for Windows we need to quote the path if it has spaces in it
	if (isWindowsShell() && (index($path, ' ') > 0))
	{
		return "\"".$path."\"";
	}

	return $path;
}

# ---------------------------------------------------------------------------- 

sub getCommandPath {
	my($usedOptionsRef,$cmdName)=@_;
	my $pgBin=$usedOptionsRef->{ $OPT_PGBIN };
	if (defined($pgBin))
	{
		my	$cmdValue=getCommandName($cmdName);
		my	$cmdPath=File::Spec->catfile($pgBin, $cmdValue);
		return adjustPath($cmdPath);
	}
	else
	{
		dieWithMessage(3, "No PG_BIN available at $pgBin");	
	}
}

# ---------------------------------------------------------------------------- 

sub appendCommandParameter {
	my($cmdValue,$optName,$usedOptionsRef)=@_;
	my $optValue=$usedOptionsRef->{ $optName };
	if (!defined($optValue))
	{
		dieWithMessage(17, "No value for option=$optName");
	}

	return $cmdValue." ".$optName."=".$optValue;	
}

# ---------------------------------------------------------------------------- 

sub appendCommonCommandParameters {
	my($cmdValue,$usedOptionsRef)=@_;

	$cmdValue = appendCommandParameter($cmdValue, $OPT_PGHOST, $usedOptionsRef);
	$cmdValue = appendCommandParameter($cmdValue, $OPT_PGPORT, $usedOptionsRef);
	$cmdValue = appendCommandParameter($cmdValue, $OPT_PGUSER, $usedOptionsRef);

	return $cmdValue;
}

sub getPsqlCommand {
	my($usedOptionsRef)=@_;
	my	$cmdValue=getCommandPath($usedOptionsRef, "psql");
	$cmdValue = appendCommonCommandParameters($cmdValue,$usedOptionsRef);
	$cmdValue = appendCommandParameter($cmdValue, $OPT_PGDB, $usedOptionsRef);
	return $cmdValue." --set ON_ERROR_STOP=on -q";
}

sub executeCommand {
	my($cmdValue)=@_;
	my $errorLine=undef;

	# redirect STDERR as well so that we can detect the ERROR message(s)
	open my $CMD, $cmdValue." 2>&1 |" || die "Could not spawn ".$cmdValue.": ".$!;
	while (defined(my $line=<$CMD> ))
	{
   		chomp($line);
   		if (($line =~ m/.*ERROR.*/) && (!defined($errorLine)))
   		{
   			$errorLine = $line;
   		}
   		print "\t".$line."\n";
	}
	close $CMD;

	return $errorLine;
}

################################ DELETE ######################################

sub deleteInternal {
	my($usedOptionsRef)=@_;
	my	$cmdValue=getCommandPath($usedOptionsRef, "dropdb");
	$cmdValue = appendCommonCommandParameters($cmdValue,$usedOptionsRef);
	
	my	$dbName=$usedOptionsRef->{ $OPT_PGDB };
	$cmdValue = $cmdValue." -w ".$dbName;
	print "Deleting $dbName database ... ";
	my $cmdResult=executeCommand($cmdValue);
	print "Done\n";
	return $cmdResult;
}

sub doDelete {
	my($usedOptionsRef)=@_;
	my $cmdResult=deleteInternal($usedOptionsRef);
	if (defined($cmdResult))
	{
		dieWithMessage(505, "Failed to delete database: $cmdResult");
	}
}

################################ POPULATE ######################################

@MODELING_SCRIPTS=(	# SQL suffix is implied - order is important (!!!)
		"DbInit",
		"ProbeInfo",
		"UserInfo",
		"ProbingScenario",
		"ProbingSchedule",
		"ProbingTaskEntry",
		"ProbingTaskResult",
		"ProbeCaptureEvent",
		"Threshold"
	);
sub runModelingScripts {
	my($cmdPrefix,$usedOptionsRef)=@_;
	my $rootLocation=$usedOptionsRef->{ $OPT_LOCATION };
	my $modelLocation=File::Spec->catfile($rootLocation, "model");
	if (! -e $modelLocation)
	{
		dieWithMessage(13, "Missing model folder $modelLocation");
	}

	foreach my $fileName (@MODELING_SCRIPTS)
	{
		my $fileLocation=File::Spec->catfile($modelLocation, $fileName.".sql");
		if (! -e $fileLocation)
		{
			dieWithMessage(14, "Missing script file $fileName in $modelLocation");
		}

		my	$scriptPath=adjustPath($fileLocation);
		print "Running $fileName script ... ";
		my $cmdResult=executeCommand($cmdPrefix.$scriptPath);
		if (defined($cmdResult))
		{
			dieWithMessage(701, "Failed to run script: $cmdResult");
		}
		print "Done\n";
	}
}

sub doModel {
	my($usedOptionsRef)=@_;
	my	$cmdValue=getPsqlCommand($usedOptionsRef);
	$cmdValue = $cmdValue." --file=";
	runModelingScripts($cmdValue, $usedOptionsRef);
}

################################ POPULATE ######################################

sub readColumnsData {
	my($colsPath)=@_;
	my $colsLine=undef;

	open my $FILEHANDLE, $colsPath || die "Failed to open \"$colsPath\": ".$!;
	while (defined(my $line=<$FILEHANDLE>))
	{
		chomp($line);

		# skip empty lines and comments
		if ((length($line) <= 0)
		 || ($line =~ m/^[ \t\n]+$/)
		 || ($line =~ m/^--.*/))
		{
			next;
		}

		if (defined($colsLine))
		{
			dieWithMessage(17, "Multiple column lines in $colsPath");
		}

		$colsLine = $line;
	}

	close $FILEHANDLE;

	if (!defined($colsLine))
	{
		dieWithMessage(17, "No column lines in $colsPath");
	}

	return $colsLine;
}

sub adjustDataFilePath {
	my($dataFilePath)=@_;
	if (isWindowsShell())
	{
		my	$escFilePath=$dataFilePath;
		$escFilePath =~ s/\\/\\\\/g;
		return "E'".$escFilePath."'";
	}
	else
	{
		return "'".$dataFilePath.".";
	}
}

sub loadDataFiles {
	my($cmdPrefix,$usedOptionsRef,$dataFolder,@filesList)=@_;
	my $rootLocation=$usedOptionsRef->{ $OPT_LOCATION };
	my $dataLocation=File::Spec->catfile($rootLocation, $dataFolder);
	if (! -e $dataLocation)
	{
		dieWithMessage(13, "Missing data folder $dataLocation");
	}

	foreach my $fileName (@filesList)
	{
		my $dataFileLocation=File::Spec->catfile($dataLocation, $fileName.".csv");
		if (! -e $dataFileLocation)
		{
			dieWithMessage(15, "Missing data file $fileName in $dataLocation");
		}

		my $colsFileLocation=File::Spec->catfile($dataLocation, $fileName.".col");
		if (! -e $colsFileLocation)
		{
			dieWithMessage(16, "Missing columns file $fileName in $dataLocation");
		}
		
		my	$colsLine=readColumnsData($colsFileLocation);
		my	$dataLoadPath=adjustDataFilePath($dataFileLocation);
		my	$cmdValue=$cmdPrefix." --command=\""
								."COPY ".$fileName." (".$colsLine.")"
								." FROM ".$dataLoadPath
								." DELIMITER ','"
								." CSV QUOTE ''''"
								."\""
								;
		print "Loading $fileName data ... ";
		my $cmdResult=executeCommand($cmdValue);
		if (defined($cmdResult))
		{
			dieWithMessage(707, "Failed to load data: $cmdResult");
		}
		print "Done\n";
	}
}

@POPULATION_FILES=(	# CSV and COL suffix(es) are implied - order is important (!!!)
		"UserInfo",
		"ProbingScenario",
		"HttpProbingScenario",
		"DnsProbingScenario",
		"ProbingSchedule",
		"DailyProbingSchedule",
		"ScheduleHourOfDay",
		"Threshold",
		"CaptureEventAnalysisThreshold",
		"PerformanceIndexEntry",
		"Threshold2Schedule"
	);
@LUT_FILES=(		# CSV and COL suffix(es) are implied - order is important (!!!)
		"Config",
		"LutAddressFamily",
		"LutUserRole",
		"LutSmtpRecipientType",
		"LutDnsProbeQueryType",
		"LutTimeUnit",
		"LutPerfIndex",
		"LutCaptureEventAnalysisType"
	);
sub runPopulationScripts {
	my($cmdPrefix,$usedOptionsRef)=@_;
	loadDataFiles($cmdPrefix, $usedOptionsRef, "lut", @LUT_FILES);
	loadDataFiles($cmdPrefix, $usedOptionsRef, "data", @POPULATION_FILES);
}

sub doPopulate {
	my($usedOptionsRef)=@_;
	runPopulationScripts(getPsqlCommand($usedOptionsRef), $usedOptionsRef);
}

################################ CREATE ######################################

sub createInternal {
	my($usedOptionsRef)=@_;
	my	$cmdValue=getCommandPath($usedOptionsRef, "createdb");
	$cmdValue = appendCommonCommandParameters($cmdValue,$usedOptionsRef);
	
	my	$dbName=$usedOptionsRef->{ $OPT_PGDB };
	$cmdValue = $cmdValue." -w ".$dbName;
	print "Creating $dbName database ... ";
	my $cmdResult=executeCommand($cmdValue);
	print "Done\n";
	return $cmdResult;
}

sub doCreate {
	my($usedOptionsRef)=@_;
	my $createResult=createInternal($usedOptionsRef);
	if (defined($createResult))
	{
		dieWithMessage(207, "Failed to create database: $createResult");
	}
	doModel($usedOptionsRef);
	doPopulate($usedOptionsRef);
}

################################ FORMAT ######################################

sub doFormat {
	my($usedOptionsRef)=@_;
	my $deleteResult=deleteInternal($usedOptionsRef);
	if (defined($deleteResult))
	{
		warn "Failed to delete database: $deleteResult";
	}
	doCreate($usedOptionsRef);
}

################################ UPDATE ######################################

sub doUpdate {
	my($usedOptionsRef)=@_;
	
	dieWithMesssage(11, "Option not available yet");
}

################################ DUMP/RESTORE ######################################

sub resolveDumpFilePath {
	my($usedOptionsRef)=@_;
	my $dumpLocation=$usedOptionsRef->{ $OPT_DUMP_FOLDER };
	if (! -e $dumpLocation)
	{
		print "Creating $dumpLocation ...";
		# TODO - does not work on Windows
		File::Path->make_path($dumpLocation);
	}

	my $fileName=$usedOptionsRef->{ $OPT_NAME };
	if (defined($fileName))
	{
		return File::Spec->catfile($dumpLocation, $fileName);
	}
	else
	{
		dieWithMessage(307, "No dump file name specified");
	}
}

sub doDump {
	my($usedOptionsRef)=@_;
	my	$cmdValue=getCommandPath($usedOptionsRef, "pg_dump");
	$cmdValue = appendCommonCommandParameters($cmdValue,$usedOptionsRef);

	my $filePath=resolveDumpFilePath($usedOptionsRef);
	$cmdValue = $cmdValue." -f ".adjustPath($filePath);

	my	$dbName=$usedOptionsRef->{ $OPT_PGDB };
	$cmdValue = $cmdValue." ".$dbName;
	print "Dumping $dbName database to $filePath ...\n";
	my $cmdResult=executeCommand($cmdValue);
	if (defined($cmdResult))
	{
		dieWithMessage(800, "Failed to dump database: $cmdResult");
	}
	print "Done\n";
}

sub doRestore {
	my($usedOptionsRef)=@_;
	my $deleteResult=deleteInternal($usedOptionsRef);
	if (defined($deleteResult))
	{
		warn "Failed to delete database: $deleteResult";
	}
	
	my $createResult=createInternal($usedOptionsRef);
	if (defined($createResult))
	{
		dieWithMessage(207, "Failed to create database: $createResult");
	}

	my $cmdValue=getPsqlCommand($usedOptionsRef);
	my $filePath=resolveDumpFilePath($usedOptionsRef);
	if (! -e $filePath)
	{
		dieWithMessage(404, "Restore file not found: $filePath");
	}
	$cmdValue = $cmdValue." --file=".adjustPath($filePath);

	print "Restoring $dbName database from $filePath ...";
	my $cmdResult=executeCommand($cmdValue);
	if (defined($cmdResult))
	{
		dieWithMessage(201, "Failed to restore database: $cmdResult");
	}
	print "Done\n";
}

##################################### MAIN ################################### 

my %usedOptions=();
parseCommandLineOptions(\%usedOptions, \@ARGV);
#while (my ($key, $value)=each %usedOptions)
#{
#	if ($key =~ m/^--.*/)
#	{
#		print "\t$key: $value\n";
#	}	
#}

my $mode=$usedOptions{$OPT_MODE};
if ($mode eq $MODE_FORMAT)
{
	doFormat(\%usedOptions);
}
elsif ($mode eq $MODE_CREATE)
{
	doCreate(\%usedOptions);
}
elsif ($mode eq $MODE_MODEL)
{
	doModel(\%usedOptions);
}
elsif ($mode eq $MODE_DATA)
{
	doPopulate(\%usedOptions);
}
elsif ($mode eq $MODE_DELETE)
{
	doDelete(\%usedOptions);
}
elsif ($mode eq $MODE_UPDATE)
{
	doUpdate(\%usedOptions);
}
elsif ($mode eq $MODE_DUMP)
{
	doDump(\%usedOptions)
}
elsif ($mode eq $MODE_RESTORE)
{
	doRestore(\%usedOptions)
}
else
{
	dieWithUsage(7, "Unknown mode: $mode");
}