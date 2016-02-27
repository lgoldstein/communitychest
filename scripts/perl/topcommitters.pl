#!/usr/bin/perl
#		This script scans a given folder recursively and attempts to display
# the top committers of a Maven module.

use File::Spec;
use File::Basename;
use File::Path;
use Cwd;
use XML::LibXML;

##################################### Globals ################################### 

$MVN_PROJECT="project";
$MVN_PARENT="parent";
$MVN_GROUPID="groupId";
$MVN_ARTIFACTID="artifactId";
$MVN_VERSION="version";
$MVN_TARGET="target";

$DEFAULT_POM_FILENAME="pom.xml";

$OPT_HELP="--help";
$OPT_VERSION="--".$MVN_VERSION;
$OPT_OUTPUT_FILE="--file";
$OPT_SCM="--scm";
	$SCM_SVN="svn";
	$SCM_GIT="git";
	$SCM_AUTO="auto";
	$DEFAULT_SCM=$SCM_AUTO;
$OPT_LOCATION="--location";
$OPT_RAW="--raw";

sub trim {
	my($string)=@_;
    if (defined($string))
    {
        $string =~ s/^\s+//;
        $string =~ s/\s+$//;
    }
	return $string;
}

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
	my($envVarName)=@_;
	if (exists($ENV{$envVarName}))
	{
		return $ENV{$envVarName};
	}

	return undef;
}

sub resolveDefaultTempFolderEnvVarName {
	if (isWindowsShell(@_))
	{
		return "TEMP";
	}
	else
	{
		return "TMPDIR";	# see http://en.wikipedia.org/wiki/TMPDIR
	}
}

##################################### Functions ################################### 

sub showUsage {
	print STDOUT "Usage: topcommitters [OPTIONS] [PATH]\n";
	print STDOUT "\n";
	print STDOUT "Where OPTIONS are: \n";
	print STDOUT "\n";
		print STDOUT "\t$OPT_OUTPUT_FILE=<file> - write results to specified file (default=STDOUT)\n";
		print STDOUT "\t$OPT_SCM=<scm> - Source Control Manager being used (default=$DEFAULT_SCM):\n";
			print STDOUT "\t\t$SCM_SVN - SubVersion\n";
			print STDOUT "\t\t$SCM_GIT - Git\n";
			print STDOUT "\t\t$SCM_AUTO - Auto-detect";	
	    print STDOUT "\t$OPT_RAW - do not detect Maven modules\n";
		print STDOUT "\t$OPT_HELP - show this help message\n";
	print STDOUT "\n";
	print STDOUT "If no path argument specified the the CWD is used\n";
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
# Parameters: current value, variable name
# Returns: if current value defined, then current value - otherwise, the value of the specified environment variable
# Exception(s): fails if specified environment variable not defined (and required to be used)
sub takeFromEnvironment {
	my($curVal,$varName)=@_;

	if (!defined($curVal))
	{
		$curVal = getEnvVarValue($varName);
		if (!defined($curVal))
		{
			dieWithUsage(404, "No ".$varName." environment definition found");
		}
	}

	return $curVal;
}

# ---------------------------------------------------------------------------- 

# Parameters: option name, alternative name, value, options hash
# If the option has been mapped by the specified name or the alternative
# one (if defined) then dies with error message. Otherwise maps the
# value to the name and its alternative (if defined)
sub updateOptionValue {
	my($optName,$altName,$optVal,$usedOptionsRef)=@_;

	# check if option re-specified
	if (exists($usedOptionsRef->{$optName}))
	{
		dieWithUsage(901, "Option $optName re-specified");
	}

	if (defined($altName) && (exists($usedOptionsRef->{$altName})))
	{
		dieWithUsage(902, "Option $altName re-specified as $optName");
	} 

	# mark the option as used
	$usedOptionsRef->{$optName} = $optVal;
	if (defined($altName))
	{
		$usedOptionsRef->{$altName} = $optVal;
	}

	return $optVal;
}

# ---------------------------------------------------------------------------- 

# Parameters: option name, value, options hash
# If specified option already mapped does nothing. Otherwise maps the value
sub updateDefaultOption {
	my($optName,$optVal,$usedOptionsRef)=@_;
	if (!exists($usedOptionsRef->{$optName}))
	{
		$usedOptionsRef->{$optName} = $optVal;	# mark the option as used
	}

	return $usedOptionsRef;
}

# ---------------------------------------------------------------------------- 

sub parseCommandLineOptions {
	my ($usedOptionsRef,$cmdArgsRef)=@_;
	my @cmdArgs=@{ $cmdArgsRef };

	while (@cmdArgs)
	{
		my	$optSpec=shift(@cmdArgs);
		# if found help request then process no more and die after the usage message
		if ($optSpec eq $OPT_HELP)
		{
			showUsage();
			die;
		}
		elsif ($optSpec eq $OPT_RAW)
		{
            updateOptionValue($optSpec, undef, "true", $usedOptionsRef);
            next;
		}

		if (index($optSpec, "--") != 0)
		{
			if ($#cmdArgs >= 0)
			{
				dieWithMessage(505, "Path argument must be last");
			}

			updateOptionValue($OPT_LOCATION, undef, $optSpec, $usedOptionsRef);
			last;
		}

		my	$argIndex=index($optSpec, '=');
		if (($argIndex < 0) && (argIndex == (length($optSpect) - 1)))
		{
			dieWithMessage(333, "No argument specified for ".$optSpec);
		}

		my	$optName=substr($optSpec, 0, $argIndex);
		my	$optVal=substr($optSpec, $argIndex + 1);
		if (($optName eq $OPT_OUTPUT_FILE)
		 || ($optName eq $OPT_SCM))
		{
			updateOptionValue($optName, undef, $optVal, $usedOptionsRef);
		}
		else
		{
			dieWithUsage(911, "Unknown argument: $optName");
		}
	}

	updateDefaultOption($OPT_SCM, $DEFAULT_SCM, $usedOptionsRef);
	updateDefaultOption($OPT_LOCATION, Cwd::realpath(getcwd()), $usedOptionsRef);

	return $usedOptionsRef;
}

# ---------------------------------------------------------------------------- 

my %SKIPPED_FILE_NAMES=(
		"."			=> "skip current entry",
	 	".."		=> "skip parent entry",
	 	".svn"		=> "skip SVN sub folder",
	 	".git"		=> "skip GIT sub folder",
	 	"src"		=> "skip sources sub folder",
	 	"bin"		=> "skip bin folder",
	 	"lib"		=> "skip lib folder",
	 	$MVN_TARGET	=> "skip target artifacts sub folder",
	 	"classes"	=> "skip target classes sub folder",
	 	".settings"	=> "skip Eclipse settings sub folder",
	 	".metadata"	=> "skip Eclipse workspace sub folder"
	);	
sub skipScannedFile {
	my($fileName) = @_;
	if (exists($SKIPPED_FILE_NAMES{ $fileName }))
	{
		return 1;
	}
	else
	{
		return 0;
	}
}

# ----------------------------------------------------------------------------

# Parameters: XML node
# Finds the 1st TEXT child node (if root node is defined)  
sub getTextNodeChild {
	my($node)=@_;
	if (!defined($node))
	{
		return undef;
	}

	foreach my $child ($node->getChildnodes())
	{
		if ($child->nodeType eq &XML_TEXT_NODE)
		{
			return $child;
		}
	}

	return undef;
}

# Parameters: document, message string, list of path elements (local names)
# Returns the one and only node that matches the path (undef if no match found)
# Dies with error if more than one match found
sub findNode {
	my($doc,$elemType,@pathElements)=@_;
	my $xpathQuery="";

	foreach my $pathElem (@pathElements)
	{
		$xpathQuery = $xpathQuery."/*[local-name()=\'".$pathElem."\']";
	}
	my @nodesList=$doc->findnodes($xpathQuery);
	my $lastNodeIndex=$#nodesList;
	if ($lastNodeIndex < 0)
	{
		return undef;
	}
	elsif ($lastNodeIndex > 0)
	{
		dieWithMessage(505, "Multiple $elemType elements defined in $pomFilePath");
 	}

	return getTextNodeChild(shift(@nodesList));
}

# ----------------------------------------------------------------------------

# Parameters: document, list of path elements (local names) under which
# 		the version element is expected 
# Returns the first <version> node text under the specified path (or undef)
# SEE findNode
sub extractVersionNode {
	my($doc,@pathElements)=@_;
	push(@pathElements, $MVN_VERSION);
	return findNode($doc, $MVN_VERSION, @pathElements);
}

# ----------------------------------------------------------------------------

# returns a list of the following: doc, groupId, artifactId
sub extractPomInformationNodes {
	my($pomFilePath)=@_;
	my $parser=XML::LibXML->new();
	my $doc=$parser->parse_file($pomFilePath);

	my $groupId=findNode($doc, $MVN_GROUPID, $MVN_PROJECT, $MVN_GROUPID);
	if (!defined($groupId))
	{
		$groupId=findNode($doc, $MVN_GROUPID, $MVN_PROJECT, $MVN_PARENT, $MVN_GROUPID);
	}

	my $artifactId=findNode($doc, $MVN_ARTIFACTID, $MVN_PROJECT, $MVN_ARTIFACTID); 
	if (!defined($artifactId))
	{
		dieWithMessage(404, "Missing main $MVN_ARTIFACTID element in $pomFilePath");
	}

	return ($doc,$groupId,$artifactId); 
}

# ---------------------------------------------------------------------------- 

# Parameters: list of TEXT nodes
# Return: a list of the nodes' text data (if encounters undef then places undef as the value)
sub extractTextNodesData {
	my(@nodesList)=@_;
	my @textsList=();
	while(@nodesList)
	{
		my	$node=shift(@nodesList);
		if (defined($node))
		{
			push(@textsList, $node->getData());
		}
		else
		{
			push(@textsList, undef);
		}
	}

	return @textsList;
}

# ---------------------------------------------------------------------------- 
# checks if a file is under SVN control
# input - file path, output - non-zero if file under SVN control
sub isSVNControlledFile {
	my($fileName)=@_;
	my $fileStatus="versioned";	# assume as default

	# redirect STDERR as well so that we can detect the "not a working copy" message(s)
	open my $PROG, "svn info --non-interactive \"$fileName\" 2>&1 |" || die "Could not execute INFO command on $fileName: ".$!;
	while(defined(my $line=<$PROG>))
	{
    	chomp($line);

		if (($line =~ m/^svn:.*is not a working copy.*/)
		 || ($line =~ m/^svn: warning: .*/)
		 || ($line =~ m/^svn: E[0-9]+: .*/)
		 || ($line =~ m/.*Not a versioned resource.*/))
		{
			$fileStatus = undef;
		}
	}
	close $PROG;

	if (defined($fileStatus))
	{
		return 1;
	}
	else
	{
		return 0;
	}
}

# ----------------------------------------------------------------------------
# returns a tuple ($name,$count,$revision)
sub processCommitterLine {
	my($line,$committersRef)=@_;
#	print STDOUT "\t$line\n";
	
	my	$nameStart=index($line, "|");
	if ($nameStart <= 0)
	{
		return (undef,undef,undef);
	}

	my	$revision=substr $line, 0, $nameStart - 1;
	$revision = trim($revision);

	my $nameEnd=index($line, "|", $nameStart + 1);
	if ($nameEnd <= $nameStart)
	{
		return (undef,undef,$revision);
	}

	my $name=substr $line, $nameStart + 1, $nameEnd - $nameStart - 1;
	$name = trim($name);
	if (length($name) <= 0)
	{
		return (undef,undef,$revision);
	}

	if (defined($committersRef))
	{
		my $count=0;
		if (exists($committersRef->{ $name }))
		{
			$count = $committersRef->{ $name };
		}
	
		$committersRef->{ $name } = ($count + 1);
		return ($name, $committersRef->{ $name }, $revision);
	}
	else
	{
		return ($name, 1, $revision);
	}
}

# ---------------------------------------------------------------------------- 
# checks if a file is under GIT control
# input - file path, output - non-zero if file under GIT control
sub isGITControlledFile {
	my($fileName)=@_;
	my $dirName=$fileName;
	if (! -d $fileName)
	{
		$dirName=dirname($fileName);
	}

	my $errorStatus=undef;	# assume as default
	my $curDir=Cwd::realpath(getcwd());
	my $lastCommitter=undef;

	chdir($dirName);
	open my $PROG, "git log --oneline -1 --format=format:\"%H | %ce | %cd | %f\" -- \"$fileName\" 2>&1 |" || die "Could not execute LOG command on $fileName: ".$!;
	while(defined(my $line=<$PROG>))
	{
    	chomp($line);
    	
    	if ($line =~ m/.*Not a git repository.*/)
    	{
    		$errorStatus = $line;
    	}
    	elsif (defined($lastCommitter))
		{
    		$errorStatus = $line;
   		}
   		else
   		{
    		my($name,$count,$revision) = processCommitterLine($line,undef);
    		if ((!defined(name)) || (!defined($count)) || (!defined($revision)))
    		{
    			$errorStatus = $line;
    		}
    		else
    		{
    			$lastCommitter = $name;
    		}
    	}
	}
	close $PROG;
	chdir($curDir);

	if ((!defined($errorStatus)) && defined($lastCommitter))
	{
		return 1;
	}
	else
	{
		return 0;
	}
}

# ---------------------------------------------------------------------------- 

sub isSCMControlledFile {
	my($scmType,$scanLocation)=@_;

	if (($SCM_SVN eq $scmType) && isSVNControlledFile($scanLocation))
	{
		return 1;
	}
	
	if (($SCM_GIT eq $scmType) && isGITControlledFile($scanLocation))
	{
		return 1;
	}
	
	return 0;
}

# ----------------------------------------------------------------------------

sub resolveSCM {
	my($scmType,$scanLocation)=@_;
	if (($SCM_SVN eq $scmType) || ($SCM_GIT eq $scmType))
	{
		return $scmType;
	}
	
	if (!($SCM_AUTO eq $scmType))
	{
		dieWithMessage(404, "Unknown SCM type: $scmType");
	}

	if (isSVNControlledFile($scanLocation))
	{
		return $SCM_SVN;
	}

	if (isGITControlledFile($scanLocation))
	{
		return $SCM_GIT;
	}
	
	dieWithMessage(505, "Cannot determine SCM of $scanLocation");
	return undef;
}

# ----------------------------------------------------------------------------

sub isMavenModuleRoot {
	my($moduleDir)=@_;
	
	my $srcDir=File::Spec->catfile($moduleDir, "src");
	my $mainDir=File::Spec->catfile($srcDir, "main");
	if (-d $mainDir)
	{
		return 1;
	}
	else
	{
		return 0;
	}
}

# ----------------------------------------------------------------------------

sub calculateSVNCommitters {
	my($moduleDir,$committersRef)=@_;

	# redirect STDERR as well so that we can detect bad data
	open my $PROG, "svn log --non-interactive -r 1:HEAD \"$moduleDir\" 2>&1 |" || die "Could not execute LOG command on $moduleDir: ".$!;
	while(defined(my $line=<$PROG>))
	{
    	chomp($line);

		if ($line !~ m/^r[0-9]+ .*/)
		{
			next;
		}

		processCommitterLine($line,$committersRef);
	}
	close $PROG;
	
	return $committersRef;
}

sub calculateGITCommitters {
	my($moduleDir,$committersRef)=@_;
	
	my $curDir=Cwd::realpath(getcwd());
	chdir($moduleDir);
	# redirect STDERR as well so that we can detect bad data
	open my $PROG, "git log --oneline --format=format:\"%H | %ce | %cd | %f\" -- \"$moduleDir\" 2>&1 |" || die "Could not execute LOG command on $moduleDir: ".$!;
	while(defined(my $line=<$PROG>))
	{
    	chomp($line);
		processCommitterLine($line,$committersRef);
	}
	close $PROG;
	chdir($curDir);
	
	return $committersRef;
}

# ----------------------------------------------------------------------------

sub calculateRawTopCommitters {
    my($scmType,$moduleDir,$OUTFILE)=@_;
    my %committers=();

    if ($scmType eq $SCM_SVN)
    {
        calculateSVNCommitters($moduleDir, \%committers);
    }
    elsif ($scmType eq $SCM_GIT)
    {
        calculateGITCommitters($moduleDir, \%committers);
    }

    while (my ($key, $value)=each %committers)
    {
        print $OUTFILE "$key,$value\n";
    }
}

sub calculateMavenTopCommitters {
	my($scmType,$scanRoot,$moduleDir,$pomFilePath,$OUTFILE)=@_;
	my($doc,$groupId,$artifactId) = extractPomInformationNodes($pomFilePath);
	($groupId,$artifactId) = extractTextNodesData($groupId, $artifactId);
	
	my %committers=();
	if ($scmType eq $SCM_SVN)
	{
		calculateSVNCommitters($moduleDir, \%committers);
	}
	elsif ($scmType eq $SCM_GIT)
	{
		calculateGITCommitters($moduleDir, \%committers);
	}
	
	my $rootLen=length($scanRoot);
	my $subPath=substr $moduleDir, $rootLen + 1;
	if (length($subPath) <= 0)
	{
		$subPath = basename($moduleDir);
	}
	$subPath =~ s/\\/\//g;
	
	while (my ($key, $value)=each %committers)
	{
		print $OUTFILE "$subPath,$groupId,$artifactId,$key,$value\n";
	}
}

# ----------------------------------------------------------------------------

sub scanDevelopmentTree {
	my($scmType,$scanRoot,$rawMode,$OUTFILE)=@_;
	my @foldersList=( $scanRoot );

	# go until exhausted all folders
	while(@foldersList)
	{
		my($scanLocation,$pomFilePath)=(shift(@foldersList), undef);
        if (defined($rawMode))
        {
        	calculateRawTopCommitters($scmType, $scanLocation, $OUTFILE);
            next;	
        }

		opendir(my $DIR, $scanLocation) || die "Error in opening dir $scanLocation: ".$!;
		while((my $fileName=readdir($DIR)))
		{
			if (skipScannedFile($fileName))
		 	{
				next;
			}

			my	$subFilePath=File::Spec->catfile($scanLocation, $fileName);
			if (!isSCMControlledFile($scmType, $subFilePath))
			{
				next;
			}

			# If found sub-folder then add it to the files list to be traversed
			if (-d $subFilePath)
			{
				push(@foldersList, $subFilePath);
				next;
			}
			
			if ($fileName eq $DEFAULT_POM_FILENAME)
			{
				if (defined($pomFilePath))
				{
					dieWithMessage(512, "Duplicate $fileName in $scanLocation");
				}
				
				$pomFilePath = $subFilePath;
				next;
			}
		}
		closedir($DIR);

		if (!defined($pomFilePath))
		{
			print STDOUT "Skip $scanLocation - no $DEFAULT_POM_FILENAME\n";
			next;
		}

 		my $moduleDir = $scanLocation;
		if (!isMavenModuleRoot($moduleDir))
		{
			print STDOUT "Skip $moduleDir - no Maven module\n";
			next;
		}
		
		print STDOUT "====================== $moduleDir START ======================\n";
		calculateMavenTopCommitters($scmType, $scanRoot, $moduleDir, $pomFilePath, $OUTFILE);
		print STDOUT "====================== $moduleDir END ======================\n";
	}
}

#################################### MAIN ################################### 

my %usedOptions=();
parseCommandLineOptions(\%usedOptions, \@ARGV);

my($scmType,$scanLocation,$outputFile)=(
		$usedOptions{ $OPT_SCM },
		$usedOptions{ $OPT_LOCATION },
		$usedOptions{ $OPT_OUTPUT_FILE }
	);

$scanLocation = File::Spec->rel2abs($scanLocation);
if (!(-d $scanLocation))
{
	dieWithMessage(601, "Scan location not a directory: $scanLocation");
}
$usedOptions{ $OPT_LOCATION } = $scanLocation;

$scmType = resolveSCM($scmType,$scanLocation);
$usedOptions{ $OPT_SCM } = $scmType;

while (my ($key, $value)=each %usedOptions)
{
	if ($key =~ m/^--.*/)
	{
		print STDOUT "\t$key: $value\n";
	}	
}

my $OUTFILE=undef;
if (defined($outputFile))
{
	 open($OUTFILE, ">", $outputFile) || die "Cannot open > $outputFile: $!";
}
else
{
	$OUTFILE = STDOUT;
}

print $OUTFILE "SubPath,GroupId,ArtifactId,Name,Count\n";
scanDevelopmentTree($scmType, $scanLocation, $usedOptions{ $OPT_RAW }, $OUTFILE);

if (defined($outputFile))
{
	close $OUTFILE;
}

