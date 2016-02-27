#!/usr/bin/perl
#
# Scans a workspace and deletes the 'target', '.project', '.classpath' and '.settings' folders/files
#

use File::Spec;
use File::Basename;
use File::Path;
use File::Remove;
use Cwd;

# Parameter: input string
# Return: trimmed string
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

##################################### Functions ################################### 

$OPT_ROOT="--root";

sub showUsage {
    print "Usage: mvncleanup [OPTIONS] [root folder]\n";
    print "\n";
    print "Where OPTIONS are: \n";
    print "\n";
        print "\t$OPT_HELP - show this help message\n";
    print "\n";
        print "\tIf no root folder is specified then CWD is used\n";
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
        if ($optSpec !~ /^\-/)
        {
            if ($#cmdArgs >= 0)
            {
                dieWithMessage(303, "Workspace location must be last");
            }
            updateOptionValue($OPT_ROOT, undef, Cwd::realpath($optSpec), $usedOptionsRef);
            last;
        }

        # if found help request then process no more and die after the usage message
        if ($optSpec eq $OPT_HELP)
        {
            showUsage();
            die;
        }
        else
        {
            dieWithUsage(911, "Unknown argument: $optName");
        }
    }

    updateDefaultOption($OPT_ROOT, Cwd::realpath(getcwd()), $usedOptionsRef);
}

# ----------------------------------------------------------------------------

my %SKIPPED_FILE_NAMES=(
        "."         => "skip current entry",
        ".."        => "skip parent entry",
        ".svn"      => "skip SVN sub folder",
        ".git"      => "skip GIT sub folder",
        "src"       => "skip sources sub folder",
        "bin"       => "skip bin folder",
        "lib"       => "skip lib folder",
        ".metadata" => "skip Eclipse workspace sub folder",
        "Servers"   => "skip Web servers folder",
        "RemoteSystemsTempFiles" => "skip Eclipse created folder"
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

sub cleanupMavenFiles {
	my @foldersList=@_;
	
    # go until exhausted all folders
    while(@foldersList)
    {
        my $scanLocation=shift(@foldersList);
        print "Scanning $scanLocation ...\n";       

        opendir(my $DIR, $scanLocation) || die "Error in opening dir $scanLocation: ".$!;
        while((my $fileName=readdir($DIR)))
        {
            if (skipScannedFile($fileName))
            {
                next;
            }

            my  $subFilePath=File::Spec->catfile($scanLocation, $fileName);
            # If found sub-folder then add it to the files list to be traversed
            if (-d $subFilePath)
            {
	            if (($fileName eq "target") || ($fileName eq ".settings"))
	            {
                    print "\t\tDelete $subFilePath\n";
	                File::Path::remove_tree($subFilePath);
	            }
	            else
	            {
                    push(@foldersList, $subFilePath);
	            }
            }
            elsif (($fileName eq ".classpath") || ($fileName eq ".project"))
            {
                print "\t\tDelete $subFilePath\n";
                if (isWindowsShell())
                {
                	# NOTE: there is an issue with 'Win32::FileOp' dependency - see http://search.cpan.org/~adamk/File-Remove-1.52/lib/File/Remove.pm
                	unlink($subFilePath);
                }
                else
                {
                    File::Remove::remove($subFilePath);
                }
                
                if (-e $subFilePath)
                {
                	dieWithMesssage(100, "Failed to delete $subFilePath");
                }
            }
        }
        closedir($DIR);
    }
}

############################# MAIN ########################################### 

my %usedOptions=();
parseCommandLineOptions(\%usedOptions, \@ARGV);
cleanupMavenFiles($usedOptions{ $OPT_ROOT });