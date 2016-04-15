#!/usr/bin/perl
# This script scans a given folder and checks if the "native" order in
# which the files are listed matches their lexicographical order. If
# not, it creates file copies according to the lexicographical order

use File::Spec;
use File::Copy;
use File::Basename;
use File::Path;
use File::Remove;
use Cwd;

##############################################################################

sub dieWithMessage {
    my($code,$msg)=@_;
    print STDERR "ERROR $code: $msg\n";
    exit $code;
}

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

sub reorderFiles {
    my($folderPath,@filesList)=@_;
    print "\tRe-arrange $folderPath\n";
    
    foreach my $fileName (@filesList) {
    	my $oldPath=File::Spec->catfile($folderPath, $fileName);
    	my $newPath=$oldPath.".bak";
    	print "\t\tRename $oldPath => $newPath\n";
    	File::Copy::move($oldPath, $newPath);
        print "\t\tCopy $newPath => $oldPath\n";
        File::Copy::copy($newPath, $oldPath);
        print "\t\tRemove $newPath\n";
        if (isWindowsShell()) {
            # NOTE: there is an issue with 'Win32::FileOp' dependency - see http://search.cpan.org/~adamk/File-Remove-1.52/lib/File/Remove.pm
            unlink($newPath);
        } else {
            File::Remove::remove($newPath);
        }
    }
}

sub checkFilesOrder {
    my($folderPath,@filesList)=@_;
    my @sortedList=sort @filesList;
    my $unmatchedIndex=undef;

    for (my $index=0; $index < $#filesList; $index++) {
    	my $orgFile=$filesList[$index];
    	my $srtFile=$sortedList[$index];
    	if ($orgFile eq $srtFile) {
    		next;
    	} else {
    		$unmatchedIndex = $index;
    		last;
    	}
    }
    
    if (!defined($unmatchedIndex)) {
    	return $unmatchedIndex;
    }

    print "\t\tMismatched value at index=$unmatchedIndex: expected=".$sortedList[$unmatchedIndex].",actual=".$filesList[$unmatchedIndex]."\n";  
    reorderFiles($folderPath, @sortedList);
    return $unmatchedIndex;
}

sub scanFolder {
    my($folderPath)=@_;
    if (! -d $folderPath)
    {
        dieWithMessage(2, "Not a folder: $folderPath");     
    }
    
    print "\tScan $folderPath\n";

    my @foldersList=();
    my @filesList=();

    opendir(my $DIR, $folderPath) || die "Error in opening dir $folderPath: ".$!;
    while((my $fileName=readdir($DIR)))
    {
        if (($fileName eq ".") || ($fileName eq ".."))
        {
            next;
        }

        my  $subFilePath=File::Spec->catfile($folderPath, $fileName);
        if (-d $subFilePath)
        {
            push(@foldersList, $subFilePath);
        }
        else
        {
            push(@filesList, $fileName);
        }       
    }
    closedir($DIR);

    if ($#filesList > 0)
    {
        checkFilesOrder($folderPath, @filesList);
    }

    return @foldersList;
}

###################################### MAIN ##################################

my @filesList=@ARGV;
if ($#filesList < 0)
{
    dieWithMessage(1, "Usage: $0 folder1 folder2 ...");
}

# go until exhausted all files
while (@filesList)
{
    my  $orgPath=shift(@filesList);
    my  $resPath=File::Spec->rel2abs($orgPath);
    my  @subFolders=scanFolder($resPath);
    if ($#subFolders >= 0)
    {
        push(@filesList,@subFolders);
    }
}
