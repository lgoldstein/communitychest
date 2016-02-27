#!/usr/bin/perl
# Synchronizes 2 SVN controlled folders (ignores non-SVN files in the source folder)

use File::Spec;
use File::Basename;
use File::Compare;
use File::Path;
use File::Copy;

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

sub svnAdd {
	my($filePath)=@_;
	
#	print "svn add -q --non-interactive $filePath";

	open my $PROG, "svn add -q --non-interactive \"$filePath\" 2>&1 |" || die "Could not execute ADD command on $filePath: ".$!;
	while(defined(my $line=<$PROG>))
	{
    	chomp($line);
    	dieWithMessage(-3, "Failed to add $filePath: $line");
	}
	close $PROG;
}

sub svnRemove {
	my($filePath)=@_;
	
#	print "svn remove -q --non-interactive $filePath";

	open my $PROG, "svn remove -q --non-interactive \"$filePath\" 2>&1 |" || die "Could not execute REMOVE command on $filePath: ".$!;
	while(defined(my $line=<$PROG>))
	{
    	chomp($line);
    	dieWithMessage(-3, "Failed to remove $filePath: $line");
	}
	close $PROG;
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

# Accumulate all SVN controlled sub-paths of the source folder
sub listSvnFiles {
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
		if (($fileName eq ".")			# skip current entry
		 || ($fileName eq "..")			# skip parent entry
		 || ($fileName eq ".svn")		# skip SVN sub folder
		 || ($fileName eq "target")		# skip target classes sub folder
		 || ($fileName eq "classes")	# skip target classes sub folder
		 || ($fileName eq ".metadata"))	# skip Eclipse workspace sub folder
	 	{
			next;
		}

		my	$subFilePath=File::Spec->catfile($srcPath, $fileName);
		if (isSVNControlledFile($subFilePath) != 0)
		{
			push(@srcFiles, $fileName);
		}
	}

	closedir($SRCDIR);
	return @srcFiles;
}

sub svnPropSet {
	my($filePath,$propName,$propValue)=@_;

	open my $PROG, "svn propset --non-interactive -q $propName $propValue \"$filePath\" |" || die "Could not execute PROPSET command on $filePath: ".$!;
	while (defined(my $line=<$PROG> ))
	{
    	chomp($line);
    	print "$line\n";
   	}
   	close $PROG;

	print "\t\tAdded $propName=$propValue\n"; 
}

sub svnPropDel {
	my($filePath,$propName)=@_;

	open my $PROG, "svn propdel --non-interactive -q $propName \"$filePath\" |" || die "Could not execute PROPDEL command on $filePath: ".$!;
	while (defined(my $line=<$PROG> ))
	{
   		chomp($line);
   		print "$line\n";
	}
	close $PROG;
	print "\t\tRemoved $propName\n"; 
}

sub svnSyncProps {
	my($srcPath,$dstPath)=@_;
	my %srcProps=getSVNFilePropertiesMap($srcPath);
	my %dstProps=getSVNFilePropertiesMap($dstPath);

	foreach my $srcName (keys %srcProps)
	{
		my $srcValue=$srcProps{ $srcName };
		if (exists($dstProps{ $srcName }))
		{
			my	$dstValue=$dstProps{ $srcName };
			if ($srcValue eq $dstValue)
			{
				next;
			}
		}
		
		svnPropSet($dstPath,$srcName,$srcValue);
	}

	foreach my $dstName (keys %dstProps)
	{
		if (exists($srcProps{ $dstName }))
		{
			next;
		}
		
		svnPropDel($dstPath, $dstName);
	}
}

# ----------------------------------------------------------------------------

sub svnSyncFolder {
	my($srcPath,$dstPath)=@_;
#	print "Source: $srcPath\n";
	print "$dstPath\n";

	my @srcFiles=listSvnFiles($srcPath);
	my %src2DstMap=();

	foreach my $srcName (@srcFiles)
	{
#		print "\tcheck $srcName\n";

		my	$srcFilePath=File::Spec->catfile($srcPath, $srcName);
		my	$dstFilePath=File::Spec->catfile($dstPath, $srcName);

		if (-e $dstFilePath)
		{
			if (isSVNControlledFile($dstFilePath) == 0)
			{
				svnAdd($dstFilePath);
			}

			if (-d $srcFilePath)
			{
				if (! -d $dstFilePath)
				{
					dieWithMessage(7, "Destination not a folder (same as source): $dstFilePath");
				}

				$src2DstMap{ $ srcName } = "sync'd"; 	
			}
			else	# simple file
			{
				if (-d $dstFilePath)
				{
					dieWithMessage(7, "Destination not a file (same as source): $dstFilePath");
				}

				# TODO for some reason we get (-1) that indicates an error
				my	$cmpResult=File::Compare::compare($srcFilePath,$dstFilePath);
				if ($cmpResult < 0)
				{
					dieWithMessage($cmpResult, "Failed to compare $srcFilePath contents");
				}
				elsif ($cmpResult == 0)
				{
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
		else	# destination does not exist
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
			svnAdd($dstFilePath);
		}
		
		if (-d $srcFilePath)
		{
			svnSyncFolder($srcFilePath, $dstFilePath);
		}
		else
		{
			svnSyncProps($srcFilePath, $dstFilePath);
		}
	}

	my @dstFiles=listSvnFiles($dstPath);
	my $displayPath=undef;
	foreach my $dstName (@dstFiles)
	{
#		print "\tValidate $dstName\n";
		if (exists($src2DstMap{ $dstName }))
		{
			next;
		}

		my	$dstFilePath=File::Spec->catfile($dstPath, $dstName);
		if (!defined($displayPath))
		{
			$displayPath = $dstPath;
			print "$displayPath\n";

		}

		print "\tRemoving $dstName\n";
		svnRemove($dstFilePath);
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

svnSyncFolder($srcPath,$dstPath);
