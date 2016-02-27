#!/usr/bin/env perl

use File::Spec;

##############################################################################
#
# This script parses the output of an AspectJ weaver log that is (usually)
# activated by adding -Dorg.aspectj.tracing.factory=default -Dorg.aspectj.tracing.enabled=true
# -Dorg.aspectj.tracing.file=...log file path... system properties definitions.
# The script analyzes the logged data and issues a titled(!) CSV 
#
##############################################################################

$OPT_HELP="--help";
$OPT_NOTITLE="--no-title";
$OPT_IGNORE_ERRORS="--silent";
$OPT_OUTPUT_FILE="-o";
$OPT_STDIN="--";

$ENTER_SIGN=">";
$EXIT_SIGN="<";

##################################### Functions ############################## 

sub showUsage {
	print STDOUT "Usage: aspectj-log-preprocess [OPTIONS] FILE\n";
	print STDOUT "\n";
	print STDOUT "Where OPTIONS are: \n";
	print STDOUT "\n";
		print STDOUT "\t$OPT_OUTPUT_FILE <file> - write results to specified file (default=STDOUT)\n";
		print STDOUT "\t$OPT_IGNORE_ERRORS - do not abort if failed to parse some lines\n";
		print STDOUT "\t$OPT_NOTITLE - do not generate a title line\n";
		print STDOUT "\t$OPT_HELP - show this help message\n";
	print STDOUT "\n";
		print STDOUT "\tIf input should be taken from STDIN then specify \"--\" as the input file\n";
}

# ---------------------------------------------------------------------------- 

sub dieWithMessage {
	my($code,$msg)=@_;
	print STDERR "ERROR $code: $msg\n\n";
	exit $code;
}

sub dieWithUsageMessage {
	my($code,$msg)=@_;
	showUsage();
	dieWithMessage($code,$msg);
}

# ---------------------------------------------------------------------------- 

sub trim {
	my($string)=@_;
	$string =~ s/^\s+//;
	$string =~ s/\s+$//;
	return $string;
}

# ---------------------------------------------------------------------------- 

# parses an "HH:MM:SS.MSEC" string into a matching 4 components list - returns empty list if failed
sub parseTimestamp {
	my($timeValue)=@_;
	my @comps=split(/:/, $timeValue);
	if ($#comps != 2)
	{
   		return ();
	}
	
	my $secAndMsec=$comps[2];
	my @subValues=split(/\./, $secAndMsec);
	if ($#subValues != 1)
	{
   		return ();
	}
	
	return ($comps[0], $comps[1], $subValues[0], $subValues[1]);
}

# parses a "timestamp ...thread-name... < or >" - returns 'undef' if failed
sub getThreadName {
	my($line)=@_;
	my $startPos=index($line, " ");
	if ($startPos <= 0)
	{
		return undef;
	}
	
	my	$endPos=index($line, $EXIT_SIGN);
	if ($endPos < 0)
	{
		$endPos = index($line, $ENTER_SIGN);
	}

	if ($endPos <= $startPos)
	{
		return undef;
	}
	
	my	$threadName=substr($line, $startPos + 1, $endPos - $startPos - 1);
	return trim($threadName);
}

# ---------------------------------------------------------------------------- 
# expects/allows "byte[NNN]"
sub parseClassSize {
	my($bytesValue)=@_;
	my $startPos=index($bytesValue, "[");
	my $endPos=index($bytesValue, "]");
	if (($startPos <= 0) || ($endPos <= $startPos))
	{
		return undef;
	}
	
	return substr($bytesValue, $startPos + 1, $endPos - $startPos - 1);
}

# ---------------------------------------------------------------------------- 

# expects/allows "className,"
sub parseClassName {
	my($className)=@_;
	my $commaPos=index($className, ",");
	if ($commaPos < 0)
	{
		return $className;
	}
	else
	{
		return substr($className, 0, $commaPos);
	}
}

# ---------------------------------------------------------------------------- 

# expects format: "HH:MM:SS.MSEC ...thread name... > org.aspectj.weaver.loadtime.Aj.preProcess org.aspectj.weaver.loadtime.Aj@NNNN className, byte[XXX], loader
sub parseStartClassProcess {
	my($threadName,$line)=@_;
   	
   	my @lineFields=split(/ /, $line);
   	if ($#lineFields < 7)
   	{
   		return ();
   	}
   	
   	my @timeStamp=parseTimestamp($lineFields[0]);
   	if ($#timeStamp < 0)
   	{
   		return ();
   	}
   	
   	while(@lineFields)
   	{
   		my $fieldValue=shift(@lineFields);
   		if ($fieldValue eq $ENTER_SIGN)
   		{
   			last;
   		}
   	}
   	
   	if ($#lineFields != 4)
   	{
   		return ();
   	}
   	
   	my($className,$beforeSize,$loaderName)=(parseClassName($lineFields[2]), parseClassSize($lineFields[3]), $lineFields[4]);
   	if ((!defined($className)) || (!defined($beforeSize)) || (!defined($loaderName)))
   	{
   		return ();
   	}
   	
   	return (\@timeStamp, $className, $beforeSize, $loaderName);
}

# ---------------------------------------------------------------------------- 

# Input: a reference (!) to a list of hours/minutes/seconds/msec.
sub toMilliseconds {
	my($timeValue)=@_;
	my($hours,$minutes,$seconds,$msec)=($$timeValue[0], $$timeValue[1], , $$timeValue[2], , $$timeValue[3]);
	return ($hours * 3600 * 1000) + ($minutes * 60 * 1000) + ($seconds * 1000) + $msec;
}

# ---------------------------------------------------------------------------- 

# Input: references (!) to lists of hours/minutes/seconds/msec.
sub calculateDuration {
	my($startTime, $endTime)=@_;
	my $tsStart=toMilliseconds($startTime);
	my $tsEnd=toMilliseconds($endTime);
	if ($tsEnd < $tsStart)
	{
		return undef;
	}
	else
	{
		return $tsEnd - $tsStart;
	}
}
# ---------------------------------------------------------------------------- 

# expects format: "HH:MM:SS.MSEC ...thread name... < org.aspectj.weaver.loadtime.Aj.preProcess byte[XXX]"
sub parseEndClassProcess {
	my($threadName,$line,$curData)=@_;
	if (!defined($curData))
	{
		return ();
	}
	
	my @enterData=@{ $curData };
	if ($#enterData <= 0)
	{
		return ();
	}
	
	my @lineFields=split(/ /, $line);
   	if ($#lineFields < 4)
   	{
   		return ();
   	}

   	my @timeStamp=parseTimestamp($lineFields[0]);
   	if ($#timeStamp < 0)
   	{
   		return ();
   	}
   	
   	while(@lineFields)
   	{
   		my $fieldValue=shift(@lineFields);
   		if ($fieldValue eq $EXIT_SIGN)
   		{
   			last;
   		}
   	}
	
	if ($#lineFields != 1)
   	{
   		return ();
   	}
	
	my $afterSize=parseClassSize($lineFields[1]);
	if (!defined($afterSize))
	{
		return ();
	}

	my $beforeTime=$enterData[0];
	my $duration=calculateDuration($beforeTime, \@timeStamp);
	if (!defined($duration))
	{
		return ();
	}

	my $beforeSize=$enterData[2];
	my $status=undef;
	if ($beforeSize eq $afterSize)
	{
		$status = "UNCHANGED";
	}
	else
	{
		$status = "MODIFIED";
	}

	# Order must match the title: Duration,Status,Class,Loader,Thread Name
	return ($duration, $status, $enterData[1], $enterData[3], $threadName);
}

# ---------------------------------------------------------------------------- 

# In case logged via Insight logger
sub reconstructLine {
	my($line,$locIndex)=@_;
	
	my	$startPart=substr($line, 0, $locIndex);
	my	@lineFields=split(/ /, $startPart);
	if ($#lineFields <= 2)
	{
		return $line;
	}

	my	$endPart=substr($line, $locIndex);
	# find first line field that can be parsed as a timestamp
	for (my $fieldIndex=$#lineFields; $fieldIndex > 0; $fieldIndex--)
	{
		my	$fieldValue=trim($lineFields[$fieldIndex]);
		my @timeStamp=parseTimestamp($lineFields[$fieldIndex]);
   		if ($#timeStamp < 0)
   		{
			next;
		}
		
		$line = $fieldValue;
		for ($fieldIndex++ ; $fieldIndex <= $#lineFields; $fieldIndex++)
		{
			$fieldValue=trim($lineFields[$fieldIndex]);
			$line = $line . " " . $fieldValue;
		}
		
		$line = $line . " " . trim($endPart);
		last;
	}

	return $line;
}

# ---------------------------------------------------------------------------- 

sub processLogFile {
	my($inputFile,$outputFile,$ignoreErrors)=@_;
	# Key=thread name, value=current processed class data
	my %threadsMap=();

	while(defined(my $line=<$inputFile>))
	{
    	chomp($line);

		#print STDOUT "Processing: $line\n";
    	
    	my $locIndex=index($line, "org.aspectj.weaver.loadtime.Aj.preProcess");
    	if ($locIndex <= 0)
    	{
    		next;
    	}

		my $sizeIndex=index($line, "byte[", $locIndex);
		if ($sizeIndex < 0)
		{
			next;
		}

		$line = trim(reconstructLine($line,$locIndex));
    	
    	my	$threadName=getThreadName($line);
    	if (!defined($threadName))
    	{
    		next;
    	}

		if (index($line, $ENTER_SIGN) > 0)
		{
			if (!exists($threadsMap{ $threadName }))
			{
				 my @curData = parseStartClassProcess($threadName,$line);
				 if ($#curData > 0)
				 {
				 	$threadsMap{ $threadName } = \@curData;
				 }
				 elsif (!defined($ignoreErrors))
				 {
				 	dieWithMessage(-6, "Failed to process start line: $line");
				 }
			}
			elsif (!defined($ignoreErrors))
			{
				dieWithMessage(-7, "Leftover state data for thread=$threadName at line: $line");
			}
		}
		elsif(index($line, $EXIT_SIGN) > 0)
		{
			my $curData=delete $threadsMap{ $threadName };
			my @procResult=parseEndClassProcess($threadName,$line,$curData);
			if ($#procResult >= 0)
			{
				my($duration,$status,$className,$loaderName,$threadId)=@procResult;
				print $outputFile "$duration,$status,$className,$loaderName,\"$threadName\"\n";
			}
			elsif (!defined($ignoreErrors))
			{
			 	dieWithMessage(-8, "Failed to process end line: $line");
			}
		}
	}
}

################################# Main #######################################

my @cmdArgs=@ARGV;
if ($#cmdArgs < 0)
{
	dieWithUsageMessage(-1, "No input specified");
}

my($inputPath,$inputFile,$outputPath,$outputFile,$withoutTitle,$ignoreErrors)=(undef,undef,undef,undef,undef,undef);
while (@cmdArgs)
{
	my	$optSpec=shift(@cmdArgs);
	# if found help request then process no more and die after the usage message
	if ($optSpec eq $OPT_HELP)
	{
		showUsage();
		die;
	}

	if ($optSpec eq $OPT_STDIN)
	{
		if ($#cmdArgs >= 0)
		{
			dieWithUsageMessage(-4, "Unexpected extra arguments");
		}
		$inputFile = STDIN;
		last;
	}
	
	if ($optSpec eq $OPT_OUTPUT_FILE)
	{
		if ($#cmdArgs < 0)
		{
			dieWithUsageMessage(-2, "Output file not specified");
		}
		
		if (defined($outputPath))
		{
			dieWithUsageMessage(-3, "Option re-specified: $optSpec");
		}
		
		$outputPath = shift(@cmdArgs);
		$outputPath = File::Spec->rel2abs($outputPath);
		open($outputFile, ">", $outputPath) || die "Cannot open > $outputPath: $!";
	}
	elsif ($optSpec eq $OPT_NOTITLE)
	{
		if (defined($withoutTitle))
		{
			dieWithUsageMessage(-3, "Option re-specified: $optSpec");
		}
		
		$withoutTitle = $optSpec;
	}
	elsif ($optSpec eq $OPT_IGNORE_ERRORS)
	{
		if (defined($ignoreErrors))
		{
			dieWithUsageMessage(-3, "Option re-specified: $optSpec");
		}
		
		$ignoreErrors = $optSpec;
	}
	elsif (index($optSpec, "-") == 0)
	{
		dieWithUsageMessage(-3, "Unknown option: $optSpec");
	}
	elsif ($#cmdArgs >= 0)	# Make sure exhausted all arguments
	{
		dieWithUsageMessage(-4, "Unexpected extra arguments");
	}
	else	# This is the input path
	{
		$inputPath = File::Spec->rel2abs($optSpec);	
		# print STDOUT "Processing $inputPath\n";
		open($inputFile, "<", $inputPath) || die "Cannot open > $inputPath: $!";
	}
}

if (!defined($inputFile))
{
	dieWithUsageMessage(-5, "No input source specified");
}

if (!defined($outputPath))
{
	$outputFile = STDOUT;
}

if (!defined($withoutTitle))
{
	print $outputFile "Duration,Status,Class,Loader,Thread Name\n";
}

processLogFile($inputFile,$outputFile,$ignoreErrors);

if (defined($inputPath))
{
	close $inputFile;
}

if (defined($outputPath))
{
	close $outputFile;
}

################################# END ########################################