#!/usr/bin/perl
# Imports an SVN controlled project into a GIT controlled one including the commit history

use File::Spec;
use File::Basename;
use File::Path qw(make_path remove_tree);
use Cwd;

#################################### Globals ################################### 

$OPT_HELP="--help";
$OPT_VERBOSE="--verbose";

sub verbose {
	my($enabled,$msg)=@_;
	if (defined($enabled))
	{
		print STDOUT ">>> $msg\n";
	}
	
	return $msg;
}

$OPT_SVNDIR="--svndir";
    $OPT_SVNSTART="--svnstart";
        $DEFAULT_SVNSTART="1";
    $OPT_SVNEND="--svnend";
$OPT_GITDIR="--gitdir";
    $OPT_GITSTART="--gitstart";
    $OPT_GITEND="--gitend";

# trims spaces on both ends of a string
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
        return "TMPDIR";    # see http://en.wikipedia.org/wiki/TMPDIR
    }
}

$TMPFOLDER_ENV_VARNAME=resolveDefaultTempFolderEnvVarName();
$OPT_TEMPDIR="--tempdir";
    $DEFAULT_TEMPDIR=File::Spec->catfile(getEnvVarValue($TMPFOLDER_ENV_VARNAME), "svn2gitimport");

##################################### Functions ################################### 

sub showUsage {
    print "Usage: svn2gitimport [OPTIONS]\n";
    print "\n";
    print "Where OPTIONS are: \n";
    print "\n";
        print "\t$OPT_SVNDIR=<folder> - root folder of SVN-controlled files\n";
        print "\t$OPT_SVNSTART/$OPT_SVNEND=revision - start/end SVN revision(s) - either (or both) can be omitted.\n";
        print "\t$OPT_GITDIR=<folder> - root folder of GIT-controlled files\n";
        print "\t$OPT_GITSTART/$OPT_GITEND=hash - start/end GIT commit hash(s) - either (or both) can be omitted.\n";
        print "\t$OPT_TEMPDIR=<folder> - temp folder where to place intermediate files (default=$DEFAULT_TEMPDIR)\n";
        print "\t$OPT_VERBOSE - be more verbose\n";
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
        $usedOptionsRef->{$optName} = $optVal;  # mark the option as used
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
        my  $optSpec=shift(@cmdArgs);
        # if found help request then process no more and die after the usage message
        if ($optSpec eq $OPT_HELP)
        {
            showUsage();
            die;
        }
        elsif ($optSpec eq $OPT_VERBOSE)
        {
            updateOptionValue($optSpec, undef, $optSpec, $usedOptionsRef);
            next;
        }

        my  $argIndex=index($optSpec, '=');
        if (($argIndex < 0) && (argIndex == (length($optSpect) - 1)))
        {
            dieWithMessage(333, "No argument specified for ".$optSpec);
        }

        my  $optName=substr($optSpec, 0, $argIndex);
        my  $optVal=substr($optSpec, $argIndex + 1);
        if (($optName eq $OPT_SVNDIR)
         || ($optName eq $OPT_SVNSTART)
         || ($optName eq $OPT_SVNEND)
         || ($optName eq $OPT_GITDIR)
         || ($optName eq $OPT_GITSTART)
         || ($optName eq $OPT_GITEND))
        {
            updateOptionValue($optName, undef, $optVal, $usedOptionsRef);
        }
        else
        {
            dieWithUsage(911, "Unknown argument: ".$optName);
        }
    }

    updateDefaultOption($OPT_TEMPDIR, $DEFAULT_TEMPDIR, $usedOptionsRef);
    updateDefaultOption($OPT_SVNSTART, $DEFAULT_SVNSTART, $usedOptionsRef);

    return $usedOptionsRef;
}

sub verifyDirLocation {
    my ($optName,$dir,$createIfNotExist,$verbose)=@_;
    if (!defined($dir)) {
    	dieWithMessage(15, "$optName option not set");
    }
    
    if (-d $dir) {
    	verbose($verbose, "$optName $dir exists");
    	return $dir;
    }
    
    if (-e $dir) {
    	dieWithMessage(16, "$optName=$dir target is not a directory");
    }
    
    if (defined($createIfNotExist)) {
    	make_path($dir);
    	verbose($verbose, "$optName target created: $dir");
    	return $dir;
    } else {
        dieWithMessage(17, "$optName=$dir target not found");
    }
}

# input - name: value
# output - [ name, value ] or undef
sub parseHeaderLine {
	my($line)=@_;

	$line = trim($line);
	if (length($line) <= 3)   # at least a:b
	{
		return undef;
	}
	
	my $sepPos=index($line, ':');
	if ($sepPos <= 0)
	{
		return undef;
	}

    my  $name=trim(substr($line, 0, $sepPos));
    my  $value=trim(substr($line, $sepPos + 1));
    return ($name, $value);	
}

# ----------------------------------------------------------------------------

sub openGitCommand {
	my($workDir,$cmd,$verbose)=@_;
    my $gitDir=File::Spec->catfile($workDir, '.git');
    my $gitCmd=verbose($verbose, "git --git-dir=\"$gitDir\" --work-tree=\"$workDir\" $cmd");

    # redirect STDERR as well
    open my $PROG, "$gitCmd 2>&1 |" || die "Could not execute $gitCmd: ".$!;
    return $PROG;
}

sub getHashValue {
	my($workDir,$cmd,$verbose)=@_;
    my $errorStatus=undef;  # assume as default
    my $lastHash=undef;
    my $PROG=openGitCommand($workDir, "log --oneline -1 --format=format:\"%H\" $cmd", $verbose);

    while(defined(my $line=<$PROG>))
    {
        chomp($line);
        
        $line = verbose($verbose, trim($line));
        if (($line =~ m/.*Not a git repository.*/) || defined($lastHash))
        {
            $errorStatus = $line;
        }
        else
        {
            $lastHash = $line;
        }
    }
    close $PROG;
	
	return ($lastHash,$errorStatus);
}

# checks if a file is under GIT control
# input - file path, output - non-zero if file under GIT control
sub isGITControlledFile {
    my($workDir,$verbose)=@_;
    my($lastHash,$errorStatus)=getHashValue($workDir, "HEAD", $verbose);

    if (defined($errorStatus) || (!defined($lastHash)))
    {
        return 0;
    }
    else
    {
    	return 1;
    }
}

# ----------------------------------------------------------------------------

# Finding the 1st GIT revision that is relevant for a branch (if not specified
# via command line):
#
#   git log -n 1 --pretty=format:%H --reverse  + parse the result
#
# NOTE: the last revision (if not specified via command line option):
#
#   git log -n 1 --pretty=format:%H HEAD     + parse the result

sub resolveGitRevisions {
    my($workDir,$revStart,$revEnd,$verbose)=@_;
    my $errorStatus=undef;

    if (!defined($revStart)) {
    	($revStart,$errorStatus) = getHashValue($workDir, "--reverse", $verbose);
    	if (defined($errorStatus)) {
    		$revStart = undef;
    	}
    }
    
    if (!defined($revEnd)) {
        ($revEnd,$errorStatus) = getHashValue($workDir, "HEAD", $verbose);
        if (defined($errorStatus)) {
            $revEnd = undef;
        }
    }

    if ((!defined($revStart)) ||(!defined($revEnd))) {
    	dieWithMessage(7365, "Cannot resolve GIT revision start or end");
    }
    
    return ($revStart, $revEnd);
}

# ----------------------------------------------------------------------------

sub openSVNCommand {
	my($cmd,$verbose)=@_;
	my $snvCmd=verbose($verbose, "svn $cmd");

    # redirect STDERR as well
    open my $PROG, "$snvCmd 2>&1 |" || die "Could not execute $snvCmd: ".$!;
    return $PROG;
}

# checks if a file/folder is under SVN control
# input - file path, output - the branch URL or undef if file/folder is
#       not SVN controlled
sub getSVNRepositoryURL {
    my($fileName,$verbose)=@_;
    my $fileStatus="versioned"; # assume as default
    my $rootURL=undef;
    my $PROG=openSVNCommand("info --non-interactive \"$fileName\"", $verbose);

    while(defined(my $line=<$PROG>))
    {
        chomp($line);

        $line = verbose($verbose, trim($line));

        if (($line =~ m/^svn:.*is not a working copy.*/)
         || ($line =~ m/^svn: warning: .*/)
         || ($line =~ m/^svn: E[0-9]+: .*/)
         || ($line =~ m/.*Not a versioned resource.*/))
        {
            $fileStatus = undef;
        }
        else
        {
        	my($name,$value)=parseHeaderLine($line);
        	if (defined($name) && defined($value) && ($name eq 'URL'))
        	{
      			$rootURL = $value;
       		}
        }
    }
    close $PROG;

    if (!defined($fileStatus))
    {
        return undef;
    }

    return $rootURL;    
}
# ----------------------------------------------------------------------------

sub getRevisionValue {
    my($svnRootURL,$range,$verbose)=@_;
    my $errorStatus=undef;
    my $revNumber=undef;
    my $PROG=openSVNCommand("log -q -r $range --limit 1 \"$svnRootURL\"", $verbose);

    while(defined(my $line=<$PROG>))
    {
        chomp($line);

        $line = verbose($verbose, trim($line));

        if ($line =~ m/^[\-]+$/)
        {
            next;
        }
        elsif ($line =~ m/^r([0-9]+) .*/)
        {
        	if (defined($revNumber)) {
        		$errorStatus = $line;
        	} else {
        		$revNumber = $1;
        	}
        } else {
        	$errorStatus = $line;
        }
    }
    close $PROG;
	
	return ($revNumber,$errorStatus);
}

# Finding the 1st SVN revision that is relevant for a branch (if not specified
# via command line option):
#
# 1. find out the SVN URL of the folder - svn info  + parse the result:
#
#       * look for the Repository Root: and Relative URL: values
#       * append to the root only the FIRST component of the relative URL (strip the preceding ^)
#
# 2. svn log -q -r 1:HEAD --limit 1 ...the SVN URL...   + parse the result
#
# NOTE: the last revision (if not specified via command line option):
#
# 1. find out the SVN URL of the folder - svn info  + parse the result
# 2. svn log -q -r HEAD ...the SVN URL...   + parse the result

sub resolveSvnRevisions {
    my($svnRootURL,$revStart,$revEnd,$verbose)=@_;
    my $errorStatus=undef;

    if (!defined($revStart)) {
        ($revStart,$errorStatus) = getRevisionValue($svnRootURL, "1:HEAD", $verbose);
        if (defined($errorStatus)) {
            $revStart = undef;
        }
    }
    
    if (!defined($revEnd)) {
        ($revEnd,$errorStatus) = getRevisionValue($svnRootURL, "HEAD", $verbose);
        if (defined($errorStatus)) {
            $revEnd = undef;
        }
    }

    if ((!defined($revStart)) ||(!defined($revEnd))) {
        dieWithMessage(3777347, "Cannot resolve SVN revision start or end");
    }
    
    return ($revStart, $revEnd);
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

my $verbose=$usedOptions{ $OPT_VERBOSE };
my($svnDir,$gitDir,$tempDir)=(
        verifyDirLocation($OPT_SVNDIR, $usedOptions{ $OPT_SVNDIR }, undef, $verbose),
        verifyDirLocation($OPT_GITDIR, $usedOptions{ $OPT_GITDIR }, undef, $verbose),
        verifyDirLocation($OPT_TEMPDIR, $usedOptions{ $OPT_TEMPDIR }, $OPT_TEMPDIR, $verbose)
    );

my $svnRootURL=getSVNRepositoryURL($svnDir, $verbose);
if(!defined($svnRootURL)) {
	dieWithMessage(55, "$svnDir is not SVN controlled");
}

if (!isGITControlledFile($gitDir, $verbose)) {
    dieWithMessage(55, "$gitDir is not GIT controlled");
}

my($svnStart,$svnEnd)=resolveSvnRevisions($svnRootURL, $usedOptions{ $OPT_SVNSTART }, $usedOptions{ $OPT_SVNEND }, $verbose); 
# my($gitStart,$gitEnd)=resolveGitRevisions($gitDir, $usedOptions{ $OPT_GITSTART }, $usedOptions{ $OPT_GITEND }, $verbose);
 