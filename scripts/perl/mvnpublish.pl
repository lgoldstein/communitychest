#!/usr/bin/perl
#		This script scans a given folder recursively and publishes the artifacts
# and their POM(s) while performing a version replacement. It supports 2 types
# of directory structures - a repository and a Maven project one. If it is
# scanning a Maven project then it expects the results to be in the 'target'
# sub-folder of the folder where the pom.xml file was found (if leaf project)

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
$MVN_PACKAGING="packaging";
	$MVN_POM="pom";
	$MVN_JAR="jar";
	$MVN_ZIP="zip";
	$MVN_WAR="war";
$MVN_SOURCES="sources";
$MVN_JAVADOC="javadoc";
$MVN_TARGET="target";
$MVN_TESTS="tests";
$MVN_TESTSOURCES="test-".$MVN_SOURCES;

$DEFAULT_POM_FILENAME="pom.xml";
$DEFAULT_POM_SUFFIX=".pom";

$OPT_HELP="--help";
$OPT_VERSION="--".$MVN_VERSION;
$OPT_URL="--url";
	$DEFAULT_REPO_URL="http://localhost:8080/artifactory/";
$OPT_REPOID="--repo-id";
	$DEFAULT_REPOID="remote-repository";
$OPT_LAYOUT="--layout";
	$DEFAULT_LAYOUT="default";
$OPT_MODE="--mode";
	$MODE_DEV="dev";
	$MODE_REPO="repo";
	$DEFAULT_MODE=$MODE_DEV;
$OPT_DRYRUN="--dry-run";
$OPT_LOCATION="--location";
$OPT_ACTION="--action";
	$ACT_INSTALL="install";
	$ACT_DEPLOY="deploy";
	$DEFAULT_ACTION=$ACT_DEPLOY;
$OPT_MVNOPTS="--mvnopt";
$OPT_REOPOGROUP="--".$MVN_GROUPID;
$OPT_RETRIES="--retries";
	$DEFAULT_RETRIES=1;
$OPT_RETRYSLEEP="--retry-sleep";
	$DEFAULT_RETRY_SLEEP=3;
$OPT_NOERRORS="--ignore-errors";
$OPT_NOSOURCES="--no-sources";
$OPT_NOJAVADOC="--no-javadoc";

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

sub resolveUsernameEnvVarName {
	if (isWindowsShell(@_))
	{
		return "USERNAME";
	}
	else
	{
		return "USER";
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

$OPT_LOCALREPO="--local-repo";
	$DEFAULT_LOCALREPO=getEnvVarValue("M2_REPO");

$USERNAME_ENV_VARNAME=resolveUsernameEnvVarName();
$DEFAULT_USERNAME=getEnvVarValue($USERNAME_ENV_VARNAME);

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

$TMPFOLDER_ENV_VARNAME=resolveDefaultTempFolderEnvVarName();
$OPT_TEMPDIR="--tempdir";
	$DEFAULT_TEMPDIR=File::Spec->catfile(getEnvVarValue($TMPFOLDER_ENV_VARNAME), "mvnpublish");

##################################### Functions ################################### 

sub showUsage {
	print "Usage: mvnpublish [OPTIONS]\n";
	print "\n";
	print "Where OPTIONS are: \n";
	print "\n";
		print "\t$OPT_URL=<url> - Remote repository URL (default=$DEFAULT_REPO_URL)\n";
		print "\t$OPT_REPOID=<id> - Remote repository ID (default=$DEFAULT_REPOID)\n";
		print "\t$OPT_LOCALREPO=<location> - Local installation repository (default=$DEFAULT_LOCALREPO)\n";
			print "\t\tUsed for $OPT_ACTION=$ACT_INSTALL and/or $OPT_MODE=$MODE_REPO\n";
		print "\t$OPT_MODE=[$MODE_DEV/$MODE_REPO] - expected artifacts structure (default=$DEFAULT_MODE)\n";
		print "\t$OPT_ACTION=[$ACT_DEPLOY/$ACT_INSTALL] - Maven method of publishing (default=$DEFAULT_ACTION)\n";
		print "\t$OPT_VERSION=<version> - modify published artifacts version to specified one\n";
		print "\t$OPT_LOCATION=<location> - root folder to scan (default=CWD)\n";
		print "\t$OPT_REOPOGROUP=<groupId> - root group to scan repository from\n";
			print "\t\tUsed for $OPT_MODE=$MODE_REPO\n";
		print "\t$OPT_TEMPDIR=<folder> - temp folder where to place intermediate files (default=$DEFAULT_TEMPDIR)\n";
		print "\t$OPT_MVNOPTS=<option> - add specified option to the Maven invocation\n";
		print "\t$OPT_RETRIES=<n> - number of retries for a $ACT_DEPLOY action (default=$DEFAULT_RETRIES)\n";
		print "\t$OPT_RETRYSLEEP=<sec.> - number of seconds between retries for a $ACT_DEPLOY action (default=$DEFAULT_RETRY_SLEEP)\n";
		print "\t$OPT_DRYRUN - show the publishing commands but do not execute them\n";
		print "\t$OPT_NOERRORS - ignore Maven action execution errors\n";
		print "\t$OPT_NOSOURCES - do not publish sources\n";
		print "\t$OPT_NOJAVADOC - do not publish javadoc\n";
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

	if ($#cmdArgs < 0)
	{
		push(@cmdArgs, $OPT_HELP);
	}

	while (@cmdArgs)
	{
		my	$optSpec=shift(@cmdArgs);
		# if found help request then process no more and die after the usage message
		if ($optSpec eq $OPT_HELP)
		{
			showUsage();
			die;
		}
		elsif (($optSpec eq $OPT_DRYRUN)
			|| ($optSpec eq $OPT_NOSOURCES)
			|| ($optSpec eq $OPT_NOJAVADOC)
		    || ($optSpec eq $OPT_NOERRORS))
		{
			updateOptionValue($optSpec, undef, $optSpec, $usedOptionsRef);
			next;
		}

		my	$argIndex=index($optSpec, '=');
		if (($argIndex < 0) && (argIndex == (length($optSpect) - 1)))
		{
			dieWithMessage(333, "No argument specified for ".$optSpec);
		}

		my	$optName=substr($optSpec, 0, $argIndex);
		my	$optVal=substr($optSpec, $argIndex + 1);
		if (($optName eq $OPT_URL)
		 || ($optName eq $OPT_VERSION)
		 || ($optName eq $OPT_MODE)
		 || ($optName eq $OPT_ACTION)
		 || ($optName eq $OPT_REPOID)
		 || ($optName eq $OPT_LAYOUT)
		 || ($optName eq $OPT_TEMPDIR)
		 || ($optName eq $OPT_LOCALREPO)
		 || ($optName eq $OPT_REOPOGROUP)
		 || ($optName eq $OPT_RETRIES)
		 || ($optName eq $OPT_RETRYSLEEP)
		 || ($optName eq $OPT_LOCATION))
		{
			updateOptionValue($optName, undef, $optVal, $usedOptionsRef);
		}
		elsif ($optName eq $OPT_MVNOPTS)
		{
			if (exists($usedOptionsRef->{ $OPT_MVNOPTS }))
			{
				$usedOptionsRef->{ $OPT_MVNOPTS } = $usedOptionsRef->{ $OPT_MVNOPTS }." ".$optVal;
			}
			else
			{
				$usedOptionsRef->{ $OPT_MVNOPTS } = $optVal;
			}
		}
		else
		{
			dieWithUsage(911, "Unknown argument: $optName");
		}
	}

	updateDefaultOption($OPT_MODE, $DEFAULT_MODE, $usedOptionsRef);
	updateDefaultOption($OPT_ACTION, $DEFAULT_ACTION, $usedOptionsRef);
	updateDefaultOption($OPT_TEMPDIR, $DEFAULT_TEMPDIR, $usedOptionsRef);
	updateDefaultOption($OPT_RETRIES, $DEFAULT_RETRIES, $usedOptionsRef);
	updateDefaultOption($OPT_RETRYSLEEP, $DEFAULT_RETRY_SLEEP, $usedOptionsRef);

	my	$scanMode=$usedOptionsRef->{ $OPT_MODE };
	if ($scanMode eq $MODE_DEV)
	{
		updateDefaultOption($OPT_LOCATION, Cwd::realpath(getcwd()), $usedOptionsRef);
	}
	elsif ($scanMode eq $MODE_REPO)
	{
		if (!exists($usedOptionsRef->{ $OPT_LOCATION }))
		{
			my	$localRepo=$DEFAULT_LOCALREPO;
			if (exists($usedOptionsRef->{ $OPT_LOCALREPO }))
			{
				$localRepo = $usedOptionsRef->{ $OPT_LOCALREPO };
			}
	
			if (exists($usedOptionsRef->{ $OPT_REOPOGROUP }))
			{
				my	@groupPath=split(/\./, $usedOptionsRef->{ $OPT_REOPOGROUP });
				foreach my $subFolder (@groupPath)
				{
					$localRepo = File::Spec->catfile($localRepo, $subFolder);
				}
			}
			else
			{
				dieWithUsage(73, $OPT_REOPOGROUP." must be specified for mode=".$scanMode + " if no location provided");
			}

			$usedOptionsRef->{ $OPT_LOCATION } = $localRepo;
		}
	}
	else
	{
		dieWithMessage(722, "Unknown/Unsupported mode: $scanMode")
	}

	my	$action=$usedOptionsRef->{ $OPT_ACTION };
	if ($action eq $ACT_DEPLOY)
	{
		updateDefaultOption($OPT_URL, $DEFAULT_REPO_URL, $usedOptionsRef);
	}

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

# returns a list of the following: doc, groupId, artifactId, mainVersion [, parentVersion or undef]
sub extractPomInformationNodes {
	my($pomFilePath)=@_;
	my $parser=XML::LibXML->new();
	my $doc=$parser->parse_file($pomFilePath);

	my $mainVersion=extractVersionNode($doc, $MVN_PROJECT);
	if (!defined($mainVersion))
	{
		dieWithMessage(404, "Missing main version element in $pomFilePath");
	}

	my $parentVersion=extractVersionNode($doc, $MVN_PROJECT, $MVN_PARENT);

	my $groupId=findNode($doc, $MVN_GROUPID, $MVN_PROJECT, $MVN_GROUPID);
	if (!defined($groupId))
	{
		dieWithMessage(404, "Missing main $MVN_GROUPID element in $pomFilePath");
	}

	my $artifactId=findNode($doc, $MVN_ARTIFACTID, $MVN_PROJECT, $MVN_ARTIFACTID); 
	if (!defined($artifactId))
	{
		dieWithMessage(404, "Missing main $MVN_ARTIFACTID element in $pomFilePath");
	}

	return ($doc,$groupId,$artifactId,$mainVersion,$parentVersion); 
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

# returns a list of the following: new POM file path,groupId, artifactId, mainVersion [, parentVersion or undef]
sub createPomFileOverride {
	my($tempDir,$fileName,$pomFilePath,$versionOverride)=@_;
	my($doc,$groupId,$artifactId,$mainVersion,$parentVersion)=extractPomInformationNodes($pomFilePath);

	$mainVersion->setData($versionOverride);
	if (defined($parentVersion))
	{
		$parentVersion->setData($versionOverride);
	}


	($groupId,$artifactId,$mainVersion,$parentVersion) =
		extractTextNodesData($groupId,$artifactId,$mainVersion,$parentVersion);
	my	$cpyFileName=$groupId."-".$artifactId."-".$versionOverride.".".$MVN_POM;
	my	$newFilePath=File::Spec->catfile($tempDir, $cpyFileName);
	$doc->toFile($newFilePath, 1);
	print "\tCreated $cpyFileName ...\n";

	return ($newFilePath,$groupId,$artifactId,$mainVersion,$parentVersion);
}

# ---------------------------------------------------------------------------- 

%MVN_ARTIFACTS=(
		".".$MVN_JAR => $MVN_JAR,
		".".$MVN_ZIP => $MVN_ZIP,
		".".$MVN_WAR => $MVN_WAR
	);

# NOTE: order is important here !!!
@MVN_EXTRA_PRODUCTS=( $MVN_TESTSOURCES, $MVN_SOURCES, $MVN_JAVADOC, $MVN_TESTS );

# Parameters: scanned folder, file path, artifacts hash
# Updates the hash according to the file's type (JAR, sources, javadoc, POM, etc.)
sub updateFileArtifact {
	my($targetLocation,$subFilePath,$artifactsRef)=@_;
	my($baseName,$dirPath,$fileType)=fileparse($subFilePath, qr/\.[^.]*/);
	if ((!defined($fileType)) || (!exists($MVN_ARTIFACTS{ $fileType })))
	{
		return $artifactsRef;	# skip if not one of the file types we look for
	}
	else
	{
		$fileType = $MVN_ARTIFACTS{ $fileType };
	}

	my	$subType=undef;
	if ($fileType eq $MVN_JAR)
	{
		foreach my $prodType (@MVN_EXTRA_PRODUCTS)
		{
			if ($baseName =~ m/.*\-$prodType$/)
			{
				if (exists($artifactsRef->{ $prodType }))
				{
					dieWithMessage(707, "Multiple artifacts of type=$prodType in $targetLocation");
				}

				$artifactsRef->{ $prodType } = $subFilePath;
				$subType = $prodType;
				return $artifactsRef;	
			}
		}
	}

	if (!defined($subType))
	{
		# if have a previous 'main' artifact then prefer it if it is NOT a jar
		if (exists($artifactsRef->{ $MVN_TARGET }))
		{
			my $prevArtifact=$artifactsRef->{ $MVN_TARGET };
			my($prevName,$prevPath,$prevType)=fileparse($prevArtifact, qr/\.[^.]*/);
			$prevType = $MVN_ARTIFACTS{ $prevType };
			if ($prevType ne $MVN_JAR)
			{
				return $artifactsRef;	
			}
		}

		$artifactsRef->{ $MVN_TARGET } = $subFilePath;
		$artifactsRef->{ $MVN_PACKAGING } = $fileType;
	}

	return $artifactsRef;
}

# ---------------------------------------------------------------------------- 

sub updateDevelopedArtifacts {
	my($scanLocation,$artifactsRef)=@_;
	my $targetLocation=File::Spec->catfile($scanLocation, $MVN_TARGET);
	if ((! -e $targetLocation) || (! -d $targetLocation))
	{
		return $artifactsRef;
	}

	opendir(my $DIR, $targetLocation) || die "Error in opening dir $targetLocation: ".$!;
	while((my $fileName=readdir($DIR)))
	{
		if (($fileName eq ".") || ($fileName eq ".."))
		{
			next;
		}

		my	$subFilePath=File::Spec->catfile($targetLocation, $fileName);
		if (! -f $subFilePath)	# we care only about files - not folders
		{
			next;
		}
		
		updateFileArtifact($targetLocation, $subFilePath, $artifactsRef);
	}
	closedir($DIR);
	
	return $artifactsRef;
}

# ----------------------------------------------------------------------------

sub executeCommand {
	my($mvnCmd,$numRetries,$retrySleep,$ignoreErrors)=@_;
	for (my $retryCount=1; $retryCount <= $numRetries; $retryCount++)
	{
		my $errorLine=undef;

		open my $CMD, $mvnCmd." |" || die "Could not spawn ".$mvnCmd.": ".$!;
		while (defined(my $line=<$CMD> ))
		{
	   		chomp($line);
	   		if (($line =~ m/^\[ERROR\].*/) && (!defined($errorLine)))
	   		{
	   			$errorLine = $line;
	   		}
	   		print "\t\t".$line."\n";
		}
		close $CMD;

		if (defined($errorLine))
		{
			if (($ignoreErrors > 0) || ($numRetries > 1))
			{
				print STDERR "(Retry $retryCount out of $numRetries): ".$errorLine."\n";
			}
			else
			{
				dieWithMessage(999, "Failed to execute ".$mvnCmd.": ".$errorLine);
			}
			
			if (defined($retrySleep) && ($retrySleep > 0) && ($retryCount < ($numRetries - 1)))
			{
				sleep($retrySleep);
			}
		}
		else
		{
			return $mvnCmd;
		}
	}

	if ($ignoreErrors > 0)
	{
		return $mvnCmd;
	}

	dieWithMessage(555, "Max. number of retries ($numRetries) exceeded for ".$mvnCmd);
}

# ----------------------------------------------------------------------------

sub appendMavenCommandOption {
	my($mvnCmd,$optName,$optVal)=@_;
	if (!defined($optVal))
	{
		dieWithMessage(701, "Missing ".$optName." value for cmd=".$mvnCmd);
	}
	return $mvnCmd." -D".$optName."=".$optVal;
}

sub appendCommandValue {
	my($mvnCmd, $artifactsRef, $key, $optName)=@_;
	return appendMavenCommandOption($mvnCmd, $optName, $artifactsRef->{ $key });
}

sub appendOptionalCommandValue {
	my($mvnCmd, $artifactsRef, $key, $optName)=@_;
	if (exists($artifactsRef->{ $key }))
	{
		return appendCommandValue($mvnCmd, $artifactsRef, $key, $optName);
	}
	else
	{
		return $mvnCmd;
	}
}

# ----------------------------------------------------------------------------

sub publishClassifiedArtifact {
	my($scanLocation,$artifactsRef,$usedOptionsRef,$effPublish,$numRetries,$retrySleep,$ignoreErrors,$mvnCmd,$artifactPath,$classifier)=@_;
	
	$mvnCmd = appendMavenCommandOption($mvnCmd, "file", $artifactPath);
	$mvnCmd = appendMavenCommandOption($mvnCmd, "classifier", $classifier);

	print "\t\t".$mvnCmd."\n";
	if ($effPublish > 0)
	{
		return executeCommand($mvnCmd, $numRetries, $retrySleep, $ignoreErrors);
	}

	return $mvnCmd;
}

sub publishArtifacts {
	my($scanLocation,$artifactsRef,$usedOptionsRef,$effPublish,$numRetries,$retrySleep,$ignoreErrors)=@_;
	my($mvnAction,$pomFilePath)=( $usedOptionsRef->{ $OPT_ACTION }, $artifactsRef->{ $MVN_POM }	);

	my	$mvnCmd="mvn -N";
	# append any extra options specified by the user
	if (exists($usedOptionsRef->{ $OPT_MVNOPTS }))
	{
		$mvnCmd = $mvnCmd." ".$usedOptionsRef->{ $OPT_MVNOPTS };
	}

	if ($mvnAction eq $ACT_INSTALL)
	{
		$mvnCmd = $mvnCmd." install:install-file";
		$mvnCmd = appendOptionalCommandValue($mvnCmd, $usedOptionsRef, $OPT_LOCALREPO, "localRepositoryPath");
	}
	elsif ($mvnAction eq $ACT_DEPLOY)
	{
		$mvnCmd = $mvnCmd." deploy:deploy-file";
		$mvnCmd = appendCommandValue($mvnCmd, $usedOptionsRef, $OPT_URL, "url");
		$mvnCmd = appendOptionalCommandValue($mvnCmd, $usedOptionsRef, $OPT_REPOID, "repositoryId");
	}
	else
	{
		dieWithMessage(101, "Unknown publishing action: $mvnAction");
	}

	# NOTE: we rely on the similarity between the install and deploy commands here
	$mvnCmd = appendMavenCommandOption($mvnCmd, "generatePom", "false");
	$mvnCmd = appendCommandValue($mvnCmd, $artifactsRef, $MVN_GROUPID, $MVN_GROUPID);
	$mvnCmd = appendCommandValue($mvnCmd, $artifactsRef, $MVN_ARTIFACTID, $MVN_ARTIFACTID);
	$mvnCmd = appendCommandValue($mvnCmd, $artifactsRef, $MVN_VERSION, $MVN_VERSION);
	$mvnCmd = appendMavenCommandOption($mvnCmd, "pomFile", $pomFilePath);
	$mvnCmd = appendOptionalCommandValue($mvnCmd, $usedOptionsRef, $OPT_LAYOUT, "repositoryLayout");

	my	$mvnCmdPrefix=$mvnCmd;
	if ($mvnAction eq $ACT_INSTALL)
	{
		if (!exists($usedOptionsRef->{ $OPT_NOSOURCES }))
		{
			$mvnCmd = appendOptionalCommandValue($mvnCmd, $artifactsRef, $MVN_SOURCES, "sources");
		}

		if (!exists($usedOptionsRef->{ $OPT_NOJAVADOC }))
		{
			$mvnCmd = appendOptionalCommandValue($mvnCmd, $artifactsRef, $MVN_JAVADOC, "javadoc");
		}
	}

	if (exists($artifactsRef->{ $MVN_TARGET }))
	{
		$mvnCmd = appendCommandValue($mvnCmd, $artifactsRef, $MVN_TARGET, "file");
		$mvnCmd = appendOptionalCommandValue($mvnCmd, $artifactsRef, $MVN_PACKAGING, $MVN_PACKAGING);
	}
	else
	{
		$mvnCmd = appendMavenCommandOption($mvnCmd, "file", $pomFilePath);
	}

	print "\t\t".$mvnCmd."\n";
	if ($effPublish > 0)
	{
		executeCommand($mvnCmd, $numRetries, $retrySleep, $ignoreErrors);
	}

	if (exists($artifactsRef->{ $MVN_TESTS }))
	{
		my	$effectivePrefix=$mvnCmdPrefix;
		if (($mvnAction eq $ACT_INSTALL) && (!exists($usedOptionsRef->{ $OPT_NOSOURCES })))
		{
			$effectivePrefix = appendOptionalCommandValue($effectivePrefix, $artifactsRef, $MVN_TESTSOURCES, "sources");
		}
		
		publishClassifiedArtifact($scanLocation, $artifactsRef, $usedOptionsRef, $effPublish,
								  $numRetries, $retrySleep, $ignoreErrors,
								  $mvnCmdPrefix,
								  $artifactsRef->{ $MVN_TESTS }, $MVN_TESTS);
	}

	# sources and javadoc require a special deployment command
	if ($mvnAction eq $ACT_DEPLOY)
	{
		if (exists($artifactsRef->{ $MVN_SOURCES }) && (!exists($usedOptionsRef->{ $OPT_NOSOURCES })))
		{
			publishClassifiedArtifact($scanLocation, $artifactsRef, $usedOptionsRef, $effPublish,
									  $numRetries, $retrySleep, $ignoreErrors,
									  $mvnCmdPrefix,
									  $artifactsRef->{ $MVN_SOURCES }, $MVN_SOURCES);
		}

		if (exists($artifactsRef->{ $MVN_JAVADOC }) && (!exists($usedOptionsRef->{ $OPT_NOJAVADOC })))
		{
			publishClassifiedArtifact($scanLocation, $artifactsRef, $usedOptionsRef, $effPublish,
									  $numRetries, $retrySleep, $ignoreErrors,
									  $mvnCmdPrefix,
									  $artifactsRef->{ $MVN_JAVADOC }, $MVN_JAVADOC);
		}

		if (exists($artifactsRef->{ $MVN_TESTSOURCES }) && (!exists($usedOptionsRef->{ $OPT_NOSOURCES })))
		{
			publishClassifiedArtifact($scanLocation, $artifactsRef, $usedOptionsRef, $effPublish,
									  $numRetries, $retrySleep, $ignoreErrors,
									  $mvnCmdPrefix,
									  $artifactsRef->{ $MVN_TESTSOURCES }, $MVN_TESTSOURCES);
		}
	}

	return $mvnCmd;
}

# ---------------------------------------------------------------------------- 

sub scanDevelopmentLocation {
	my($scanRoot,$versionOverride,$effPublish,$tempDir,$numRetries,$retrySleep,$ignoreErrors,$usedOptionsRef)=@_;
	my @foldersList=( $scanRoot );

	# go until exhausted all folders
	while(@foldersList)
	{
		my($scanLocation,$pomFilePath)=(shift(@foldersList), undef);
		print "Scanning $scanLocation ...\n";		

		opendir(my $DIR, $scanLocation) || die "Error in opening dir $scanLocation: ".$!;
		while((my $fileName=readdir($DIR)))
		{
			if (skipScannedFile($fileName))
		 	{
				next;
			}

			my	$subFilePath=File::Spec->catfile($scanLocation, $fileName);
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
			next;
		}

		my($doc,$groupId,$artifactId,$mainVersion,$parentVersion)=(undef,undef,undef,undef,undef,undef);
		if (defined($versionOverride))
		{
			($pomFilePath,$groupId,$artifactId,$mainVersion,$parentVersion) =
					createPomFileOverride($tempDir, $DEFAULT_POM_FILENAME, $pomFilePath, $versionOverride);
		}
		else
		{
			($doc,$groupId,$artifactId,$mainVersion,$parentVersion) = extractPomInformationNodes($pomFilePath);
			($groupId,$artifactId,$mainVersion,$parentVersion) =
				extractTextNodesData($groupId, $artifactId, $mainVersion, $parentVersion);
		}
		
		my %artifacts=(
				$MVN_POM => $pomFilePath,
				$MVN_GROUPID => $groupId,
				$MVN_ARTIFACTID => $artifactId,
				$MVN_VERSION => $mainVersion
			);
		my $artifactsRef=updateDevelopedArtifacts($scanLocation, \%artifacts, $usedOptionsRef);
		publishArtifacts($scanLocation, \%artifacts, $usedOptionsRef, $effPublish, $numRetries, $retrySleep, $ignoreErrors);
	}
}

# ---------------------------------------------------------------------------- 

sub scanRepositoryLocation {
	my($scanRoot,$versionOverride,$effPublish,$tempDir,$numRetries,$retrySleep,$ignoreErrors,$usedOptionsRef)=@_;
	my @foldersList=( $scanRoot );
	# go until exhausted all folders
	while(@foldersList)
	{
		my $scanLocation=shift(@foldersList);
		my $pomFilePath=undef;
		my %artifacts=();
		print "Scanning $scanLocation ...\n";		

		opendir(my $DIR, $scanLocation) || die "Error in opening dir $scanLocation: ".$!;
		while((my $fileName=readdir($DIR)))
		{
			if (($fileName eq ".") || ($fileName eq ".."))
		 	{
				next;
			}

			my	$subFilePath=File::Spec->catfile($scanLocation, $fileName);
			# If found sub-folder then add it to the list to be traversed
			if (-d $subFilePath)
			{
				push(@foldersList, $subFilePath);
				next;
			}
			
			if ($fileName =~ m/.*\.$MVN_POM$/)
			{ 
				if (defined($pomFilePath))
				{
					dieWithMessage(303, "Multiple POM files found in ".$scanLocation);
				}
				
				$pomFilePath = $subFilePath;
				next;
			}


			updateFileArtifact($scanLocation, $subFilePath, \%artifacts);
		}
		closedir($DIR);

		if (!defined($pomFilePath))
		{
			next;
		}

		my($groupId,$artifactId,$mainVersion,$parentVersion)=(undef,undef,undef,undef,undef);
		if (defined($versionOverride))
		{
			($pomFilePath,$groupId,$artifactId,$mainVersion,$parentVersion) =
					createPomFileOverride($tempDir, $DEFAULT_POM_FILENAME, $pomFilePath, $versionOverride);
		}
		else
		{
			my	$doc=undef;
			($doc,$groupId,$artifactId,$mainVersion,$parentVersion) = extractPomInformationNodes($pomFilePath);
			($groupId,$artifactId,$mainVersion,$parentVersion) =
				extractTextNodesData($groupId, $artifactId, $mainVersion, $parentVersion);
		}
		
		$artifacts{ $MVN_POM } = $pomFilePath;
		$artifacts{ $MVN_GROUPID } = $groupId;
		$artifacts{ $MVN_ARTIFACTID } = $artifactId;
		$artifacts{ $MVN_VERSION } = $mainVersion;
		publishArtifacts($scanLocation, \%artifacts, $usedOptionsRef, $effPublish, $numRetries, $retrySleep, $ignoreErrors);
	}
}

##################################### MAIN ################################### 

my %usedOptions=();
parseCommandLineOptions(\%usedOptions, \@ARGV);
while (my ($key, $value)=each %usedOptions)
{
	if ($key =~ m/^--.*/)
	{
		print "\t$key: $value\n";
	}
}

my($scanMode,$scanLocation,$tempDir)=(
		$usedOptions{ $OPT_MODE },
		$usedOptions{ $OPT_LOCATION },
		$usedOptions{ $OPT_TEMPDIR }
	);

if (-f $scanLocation)
{
	dieWithMessage(601, "Scan location not a directory: $scanLocation");
}

my	$versionOverride=undef;
if (exists($usedOptions{ $OPT_VERSION }))
{
	$versionOverride = $usedOptions{ $OPT_VERSION };
}

my	$effPublish=1;
if (exists($usedOptions{ $OPT_DRYRUN }))
{
	$effPublish = 0;
}
	
# create the temp folder if not already exists
if (-e $tempDir)
{
	if (-f $tempDir)
	{
		dieWithMessage(55, "Specified ".$OPT_TEMPDIR." option is not a folder: ".$tempDir);
	}
}
else
{
	mkpath([ $tempDir ], 1, 0777);
}

my($ignoreErrors,$numRetries,$retrySleep)=(0, $usedOptions{ $OPT_RETRIES }, $usedOptions{ $OPT_RETRYSLEEP });
if (exists($usedOptions{ $OPT_NOERRORS }))
{
	$ignoreErrors = 1;
}

if ($scanMode eq $MODE_DEV)
{
	scanDevelopmentLocation($scanLocation, $versionOverride, $effPublish, $tempDir, $numRetries, $retrySleep, $ignoreErrors, \%usedOptions); 
}
elsif ($scanMode eq $MODE_REPO)
{
	scanRepositoryLocation($scanLocation, $versionOverride, $effPublish, $tempDir, $numRetries, $retrySleep, $ignoreErrors, \%usedOptions); 
}
else
{
	dieWithMessage(722, "Unknown/Unsupported mode: $scanMode")
}