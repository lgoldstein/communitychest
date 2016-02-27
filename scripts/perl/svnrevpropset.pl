#!/usr/bin/perl
# Script version $Rev: 630 $

use File::Spec;
use File::Basename;

sub trim {
	my($string)=@_;
	$string =~ s/^\s+//;
	$string =~ s/\s+$//;
	return $string;
}

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

# retrieves an SVN controlled file's properties
# input - file path, output - list of property 'pairs' strings
sub getSVNFilePropertiesList {
	my($filePath)=@_;
	my(@defProps)=();

	open my $PROPLIST, "svn proplist --non-interactive -q \"$filePath\"  |" || die "Could not execute PROPLIST command on $filePath: ".$!;
	while (defined(my $line=<$PROPLIST> ))
	{
   		chomp($line);
   		unshift(@defProps, trim($line));
	}
	close $PROPLIST;

	return @defProps;
}

# retrieves an SVN controlled file's properties
# input - file path, output - associative array of properties (key=property name, value=property value])
# see getSVNFilePropertiesList
sub getSVNFilePropertiesMap {
	my($filePath)=@_;
	my @defProps=getSVNFilePropertiesList($filePath);

	my %defPropsMap=();
	foreach my $propName (@defProps)
	{
		open my $PROPVAL, "svn propget --non-interactive $propName \"$filePath\"  |" || die "Could not execute PROPGET $propName command on $filePath: ".$!;
		if (defined(my $line=<$PROPVAL>))
		{
			chomp($line);
			my $propVal=trim($line);
			$defPropsMap{ $propName } = $propVal;	
		}
		close $PROPVAL;
	}

	return %defPropsMap;
}

# checks if a file contains the $Rev keyword in the first scan lines
# input - file path + num. scan lines, output - non-zero if keyword found 
sub containsRevProperty {
	my($filePath,$numScanLines)=@_;
	my($lineCount,$revFound)=(0, 0);
	
	open my $FILEHANDLE, $filePath || die "Failed to open \"$filePath\": ".$!;

	while (defined(my $line=<$FILEHANDLE>))
	{
		chomp($line);

		if ($line =~ m/.*\$Rev.*/)
		{
			$revFound = 1;
			break;
		}

		$lineCount++;	
		if (($numScanLines > 0) && ($lineCount >= numScanLines))
		{
			break;
		}
	}
	close $FILEHANDLE;

	return $revFound;
}

# checks if a file requires update of the $Rev property
# input - file path + num. scan lines, output - zero=nothing to do, positive=add $Rev, negative=remove $Rev 
sub checkFileSettings {
	my($filePath,$numScanLines)=@_;
	if (isSVNControlledFile($filePath) == 0)
	{
		return 0;
	}

	print "\tChecking " . $filePath . "\n";

	my %fileProps=getSVNFilePropertiesMap($filePath);
	my $kwdExists=0;
	if (exists $fileProps{ "svn:keywords" })
	{
		my	$kwdValue=$fileProps{ "svn:keywords" };
		if ($kwdValue =~ m/.*Rev.*/)
		{
			$kwdExists = 1;
		}
	}

	if (containsRevProperty($filePath, $numScanLines) == 0)
	{
		# if not found property but keyword exists need to remove it
		if ($kwdExists != 0)
		{
			return (-1);
		}

		return 0;
	}

	# if found property and keyword exists then do nothing
	if ($kwdExists != 0)
	{
		return 0;
	}

	return 1;
}

# Updates the $Rev property
# input - file path + operation code (zero=do nothing, positive=add, negative=remove)
sub svnPropSet {
	my($filePath,$opRes)=@_;
	if ($opRes > 0)
	{
		open my $PROG, "svn propset --non-interactive -q svn:keywords Rev \"$filePath\" |" || die "Could not execute PROPSET command on $filePath: ".$!;
		while (defined(my $line=<$PROG> ))
		{
     		chomp($line);
     		print "$line\n";
   		}
   		close $PROG;

		print "\t\tAdded revision property: " . $filePath . "\n"; 
	}
	elsif ($opRes < 0)
	{
		open my $PROG, "svn propdel --non-interactive -q svn:keywords \"$filePath\" |" || die "Could not execute PROPDEL command on $filePath: ".$!;
		while (defined(my $line=<$PROG> ))
		{
     		chomp($line);
     		print "$line\n";
   		}
   		close $PROG;

		print "\t\tRemoved revision property: " . $filePath . "\n"; 
	}
}

############################## MAIN ######################################

my @filesList=@ARGV;
my $numScanLines=10;

if ($#filesList < 0)
{
	die "Usage: svnrevpropset [OPTIONS] file1 file2 ...\n"
		. "\tNOTE: file also means folder - in which case it will be scanned recursively\n\n"
		. "\tWhere available options are:\n\n"
		. "\t\t-l <n> - check only the 1st <n> lines of each file (default=" . $numScanLines . ")\n"
		;
}

# go until exhausted all files
while (@filesList)
{
	my $filePath=shift(@filesList);
	if (-f $filePath)
	{
		my $opRes=checkFileSettings($filePath, $numScanLines);
		if ($opRes != 0)
		{
			svnPropSet($filePath, $opRes);
		}
		next;
	}

	if (! -d $filePath)
	{
		next;
	}

	# read all sub-folders into a list to avoid having too many open DIR(s) due to recursion depth
	opendir(my $DIR, $filePath) || die "Error in opening dir $filePath: ".$!;
	# Accumulate all files for which there is an SVN setting in this hash
	my %subFilesSettings=();
	while((my $fileName=readdir($DIR)))
	{
		# TODO use a @ignoredPatterns list
		if (($fileName eq ".")			# skip current entry
		 || ($fileName eq "..")			# skip parent entry
		 || ($fileName eq ".svn")		# skip SVN sub folder
		 || ($fileName eq "target")		# skip target classes sub folder
		 || ($fileName eq "classes")	# skip target classes sub folder
		 || ($fileName eq ".metadata"))	# skip Eclipse workspace sub folder
	 	{
			next;
		}

		my	$subFilePath=File::Spec->catfile($filePath, $fileName);
		# If found sub-folder then add it to the files list to be traversed
		if (-d $subFilePath)
		{
			push(@filesList, $subFilePath);
			next;
		}

		if (-f $subFilePath)
		{
			my $opRes=checkFileSettings($subFilePath, $numScanLines);
			if ($opRes != 0)
			{
				$subFilesSettings{ $subFilePath } = $opRes;
			}
		}
	}
	closedir($DIR);

	while(my($subFilePath, $opRes)=each(%subFilesSettings))
	{
		svnPropSet($subFilePath, $opRes);
	} 
}
