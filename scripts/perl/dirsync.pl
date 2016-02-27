#!/usr/bin/perl
# Synchronizes 2 directories by making the destination one mirror EXACTLY the source one

use File::Spec;
use File::Basename;
use File::Compare;
use File::Path;
use File::Copy;
use File::Remove;

sub dieWithMessage {
    my($code,$msg)=@_;
    print STDERR "ERROR $code: $msg\n\n";
    exit $code;
}

sub trim {
    my($string)=@_;
    $string =~ s/^\s+//;
    $string =~ s/\s+$//;
    return $string;
}

# ----------------------------------------------------------------------------
# Accumulate all sub-paths of the source folder
sub listFiles {
    my($srcPath)=@_;
    if (! -d $srcPath)
    {
        dieWithMessage(5, "Not a folder: $srcPath");
    }

    opendir(my $SRCDIR, $srcPath) || die "Error in opening dir $srcPath: ".$!;
    my @srcFiles=();
    while((my $fileName=readdir($SRCDIR)))
    {
        # TODO use a @ignoredPatterns list
        if (($fileName eq ".")          # skip current entry
         || ($fileName eq ".."))         # skip parent entry
        {
            next;
        }

        push(@srcFiles, $fileName);
    }

    closedir($SRCDIR);
    return @srcFiles;
}

# ----------------------------------------------------------------------------

sub compareFiles {
	my($srcFilePath,$dstFilePath)=@_;
	my($devSrc,$inoSrc,$modeSrc,$nlinkSrc,$uidSrc,$gidSrc,$rdevSrc,$sizeSrc,$atimeSrc,$mtimeSrc,$ctimeSrc,$blksizeSrc,$blocksSrc)=stat($srcFilePath);
    my($devDst,$inoDst,$modeDst,$nlinkDst,$uidDst,$gidDst,$rdevDst,$sizeDst,$atimeDst,$mtimeDst,$ctimeDst,$blksizeDst,$blocksDst)=stat($dstFilePath);
    
    if ($sizeSrc != $sizeDst) {
    	return 1;
    }
    
    # Same size but source is less recent than destination
    if ($mtimeSrc <= $mtimeDst) {
    	return 0;
    }

    return File::Compare::compare($srcFilePath,$dstFilePath)
}

sub syncFolder {
    my($srcPath,$dstPath)=@_;
#   print "Source: $srcPath\n";
    print "$dstPath\n";

    my @srcFiles=listFiles($srcPath);
    my %src2DstMap=();

    foreach my $srcName (@srcFiles)
    {
#       print "\tcheck $srcName\n";

        my  $srcFilePath=File::Spec->catfile($srcPath, $srcName);
        my  $dstFilePath=File::Spec->catfile($dstPath, $srcName);

        if (-e $dstFilePath)
        {
            if (-d $srcFilePath)
            {
                if (! -d $dstFilePath)
                {
                    dieWithMessage(7, "Destination not a folder (same as source): $dstFilePath");
                }

                $src2DstMap{ $ srcName } = "sync'd";    
            }
            else    # simple file
            {
                if (-d $dstFilePath)
                {
                    dieWithMessage(7, "Destination not a file (same as source): $dstFilePath");
                }

                # TODO for some reason we get (-1) that indicates an error
                my  $cmpResult=compareFiles($srcFilePath,$dstFilePath);
                if ($cmpResult < 0)
                {
                    dieWithMessage($cmpResult, "Failed to compare $srcFilePath contents");
                }
                elsif ($cmpResult == 0)
                {
                    print "\tSkip $srcName\n";
                    $src2DstMap{ $ srcName } = "skipped";   
                }
                else
                {
                    print "\tUpdating $srcName\n";
                    File::Copy::copy($srcFilePath,$dstFilePath,4096) or die "Copy failed: $!";
                    $src2DstMap{ $ srcName } = "updated";   
                }
            }
        }
        else    # destination does not exist
        {
            if (-d $srcFilePath)
            {
                print "\tCreating $srcName\n";
                mkdir $dstFilePath;
                $src2DstMap{ $ srcName } = "created";   
            } 
            else
            {
                print "\tAdding $srcName\n";
                File::Copy::copy($srcFilePath,$dstFilePath,4096) or die "Copy failed: $!";
                $src2DstMap{ $ srcName } = "added";
            }
        }
        
        if (-d $srcFilePath)
        {
            syncFolder($srcFilePath, $dstFilePath);
        }
    }

    my @dstFiles=listFiles($dstPath);
    my $displayPath=undef;
    foreach my $dstName (@dstFiles)
    {
#       print "\tValidate $dstName\n";
        if (exists($src2DstMap{ $dstName }))
        {
            next;
        }

        my  $dstFilePath=File::Spec->catfile($dstPath, $dstName);
        if (!defined($displayPath))
        {
            $displayPath = $dstPath;
            print "$displayPath\n";

        }

        print "\tRemoving $dstName\n";
        File::Remove::remove($dstFilePath);
    }
}

############################## MAIN ######################################

my($srcFile,$dstFile)=@ARGV;
if (!defined($srcFile))
{
    dieWithMessage(1, "No source file");
}

if (!defined($dstFile))
{
    dieWithMessage(2, "No destination file");
}

my $srcPath=File::Spec->rel2abs($srcFile);
if (! -d $srcPath)
{
    dieWithMessage(3, "Source is not a folder: $srcPath");
}
    
my $dstPath=File::Spec->rel2abs($dstFile);
if ((-e $dstPath) && (! -d $dstPath))
{
    dieWithMessage(3, "Destination is not a folder: $srcPath");
} 

syncFolder($srcPath,$dstPath);
