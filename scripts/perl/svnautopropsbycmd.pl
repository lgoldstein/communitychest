#!/usr/bin/perl
# Script revision $Rev: 653 $

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

# params: file name, associations hashes references
# returns the required properties if any match found - null/empty otherwise
sub checkFileSettings {
	my($fileName,$sfxPropsRef,$namePropsRef)=@_;
	if (isSVNControlledFile($fileName) == 0)
	{
		return "";
	}

	my %sfxPropsSettings=%{$sfxPropsRef};
	my %namePropsSettings=%{$namePropsRef};
	my($baseName,$dirPath,$fileType)=fileparse($fileName, qr/\.[^.]*/);
	# check first if a known file name
	my $fullName=$baseName . $fileType;

	while(my($propPattern,$propValues)=each(%namePropsSettings))
	{
		if ($fullName =~ m/$propPattern/)
		{
			return $propValues;
		}
	}

	while(my($propPattern,$propValues)=each(%sfxPropsSettings))
	{
		if ($fileType =~ m/$propPattern/)
		{
			return $propValues;
		}
	}

	print "\tChecked " . $fileName . "\n";
	return "";
}

sub svnPropsCheck {
	my($filePath,@propPairs)=@_;
	my(@defProps)=();

	open my $PROPLIST, "svn proplist --non-interactive -q \"$filePath\"  |" || die "Could not execute PROPLIST command on $filePath: ".$!;
	while (defined(my $line=<$PROPLIST> ))
	{
   		chomp($line);
   		unshift(@defProps, trim($line));
	}
	close $PROPLIST;

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

	my @filteredProps=();
	foreach my $pp (@propPairs)
	{
		my($propName,$propVal)=split(/=/,$pp);
		$propName = trim($propName);
		
		if (exists $defPropsMap{ $propName })
		{
			$propVal = trim($propVal);

			my $curVal=$defPropsMap{ $propName };
			if ($curVal eq $propVal)
			{
				next;
			}
			
			unshift(@filteredProps, $pp);
		}
		else
		{
			unshift(@filteredProps, $pp);
		}
	}

	return @filteredProps;
}

# params = full file path, properties
sub svnPropSet {
	my($filePath,$fileProps)=@_;
#	print "\tProcessing " . $filePath . "\n";

	my(@propPairs)=svnPropsCheck($filePath, split(/;/, $fileProps));
	if ($#propPairs < 0)
	{
#		print "\tSkip " . $filePath . "\n";
		return;
	}

	foreach my $pp (@propPairs)
	{
		my($propName,$propVal)=split(/=/,$pp);

		open my $PROG, "svn propset --non-interactive -q $propName $propVal \"$filePath\" |" || die "Could not execute PROPSET command on $filePath: ".$!;
		while (defined(my $line=<$PROG> ))
		{
     		chomp($line);
     		print "$line\n";
   		}
   		close $PROG;
	}

	print $filePath . " " . $fileProps . "\n";
}

############################## MAIN ######################################

my %bySuffixAutoPropsSettings=(
		# Code formats
		".*\.c\$"          => "svn:eol-style=native; svn:mime-type=text/plain",
		".*\.cpp\$"        => "svn:eol-style=native; svn:mime-type=text/plain",
		".*\.h\$"          => "svn:eol-style=native; svn:mime-type=text/plain",
		".*\.java\$"       => "svn:eol-style=native; svn:mime-type=text/plain",
		".*\.groovy\$"     => "svn:eol-style=native; svn:mime-type=text/plain; svn:executable=ON",
		".*\.aj\$"         => "svn:eol-style=native; svn:mime-type=text/plain",
		".*\.as\$"         => "svn:eol-style=native; svn:mime-type=text/plain",
		".*\.cgi\$"        => "svn:eol-style=native; svn-mine-type=text/plain",
		".*\.js\$"         => "svn:eol-style=native; svn:mime-type=text/javascript",
		".*\.jsp\$"        => "svn:eol-style=native; svn:mime-type=application/jsp",
		".*\.php\$"        => "svn:eol-style=native; svn:mime-type=text/x-php",
		".*\.pl\$"         => "svn:eol-style=native; svn:mime-type=text/x-perl; svn:executable=ON",
		".*\.pm\$"         => "svn:eol-style=native; svn:mime-type=text/x-perl",
		".*\.py\$"         => "svn:eol-style=native; svn:mime-type=text/x-python; svn:executable=ON",
		".*\.sh\$"         => "svn:eol-style=LF; svn:mime-type=text/x-sh; svn:executable=ON",
		".*\.bat\$"        => "svn:eol-style=CRLF; svn:mime-type=text/plain",
		".*\.vbs\$"        => "svn:eol-style=CRLF; svn:mime-type=text/plain",
		".*\.cs\$"         => "svn:eol-style=CRLF; svn:mime-type=text/plain",
		".*\.rc\$"         => "svn:eol-style=CRLF; svn:mime-type=text/plain",
	
		# Image formats
		".*\.bmp\$"        => "svn:mime-type=image/bmp",
		".*\.gif\$"        => "svn:mime-type=image/gif",
		".*\.ico\$"        => "svn:mime-type=image/ico",
		".*\.jpeg\$"       => "svn:mime-type=image/jpeg",
		".*\.jpg\$"        => "svn:mime-type=image/jpeg",
		".*\.png\$"        => "svn:mime-type=image/png",
		".*\.tif\$"        => "svn:mime-type=image/tiff",
		".*\.tiff\$"       => "svn:mime-type=image/tiff",
	
		# Data formats
		".*\.pdf\$"        => "svn:mime-type=application/pdf",
		".*\.avi\$"        => "svn:mime-type=video/avi",
		".*\.doc\$"        => "svn:mime-type=application/msword",
		".*\.eps\$"        => "svn:mime-type=application/postscript",
		".*\.gz\$"         => "svn:mime-type=application/gzip",
		".*\.mov\$"        => "svn:mime-type=video/quicktime",
		".*\.mp3\$"        => "svn:mime-type=audio/mpeg",
		".*\.ppt\$"        => "svn:mime-type=application/vnd.ms-powerpoint",
		".*\.ps\$"         => "svn:mime-type=application/postscript",
		".*\.psd\$"        => "svn:mime-type=application/photoshop",
		".*\.rtf\$"        => "svn:mime-type=text/rtf",
		".*\.swf\$"        => "svn:mime-type=application/x-shockwave-flash",
		".*\.tgz\$"        => "svn:mime-type=application/gzip",
		".*\.wav\$"        => "svn:mime-type=audio/wav",
		".*\.xls\$"        => "svn:mime-type=application/vnd.ms-excel",
		".*\.zip\$"        => "svn:mime-type=application/zip",
	
		# Text formats
		".*\.properties\$" => "svn:eol-style=native; svn:mime-type=text/plain",
		".*\.css\$"        => "svn:eol-style=native; svn:mime-type=text/css",
		".*\.csv\$"        => "svn:eol-style=native; svn:mime-type=text/csv",
		".*\.dtd\$"        => "svn:eol-style=native; svn:mime-type=text/xml",
		".*\.html\$"       => "svn:eol-style=native; svn:mime-type=text/html",
		".*\.htm\$"        => "svn:eol-style=native; svn:mime-type=text/html",
		".*\.ini\$"        => "svn:eol-style=native; svn:mime-type=text/plain",
		".*\.sql\$"        => "svn:eol-style=native; svn:mime-type=text/x-sql",
		".*\.txt\$"        => "svn:eol-style=native; svn:mime-type=text/plain",
		".*\.xhtml\$"      => "svn:eol-style=native; svn:mime-type=text/xhtml+xml",
		".*\.xml\$"        => "svn:eol-style=native; svn:mime-type=text/xml",
		".*\.mxml\$"       => "svn:eol-style=native; svn:mime-type=text/xml",
		".*\.xsd\$"        => "svn:eol-style=native; svn:mime-type=text/xml",
		".*\.xsl\$"        => "svn:eol-style=native; svn:mime-type=text/xml",
		".*\.xslt\$"       => "svn:eol-style=native; svn:mime-type=text/xml",
		".*\.xul\$"        => "svn:eol-style=native; svn:mime-type=text/xul",
		".*\.yml\$"        => "svn:eol-style=native; svn:mime-type=text/plain"
		);

my %byBaseNameAutoPropsSettings=(	
		# Well-known used file names
		"^\\.htaccess"    => "svn:eol-style=native; svn:mime-type=text/plain",
		"^\\.project\$"   => "svn:eol-style=native; svn:mime-type=text/xml",
		"^\\.classpath\$" => "svn:eol-style=native; svn:mime-type=text/xml",
		"^AUTHORS\$"      => "svn:eol-style=native; svn:mime-type=text/plain",
		"^BUGS\$"         => "svn:eol-style=native; svn:mime-type=text/plain",
		"^CHANGES.*"      => "svn:eol-style=native; svn:mime-type=text/plain",
		"^COPYING.*"      => "svn:eol-style=native; svn:mime-type=text/plain",
		"^DEPENDENCIES\$" => "svn:eol-style=native; svn:mime-type=text/plain",
		"^DEPRECATED\$"   => "svn:eol-style=native; svn:mime-type=text/plain",
		"^INSTALL.*"      => "svn:eol-style=native; svn:mime-type=text/plain",
		"^LICENSE.*"      => "svn:eol-style=native; svn:mime-type=text/plain",
		"^Makefile.*"     => "svn:eol-style=native; svn:mime-type=text/plain",
		"^MANIFEST.*"     => "svn:eol-style=native; svn:mime-type=text/plain",
		"^PLATFORMS"      => "svn:eol-style=native; svn:mime-type=text/plain",
		"^README.*"       => "svn:eol-style=native; svn:mime-type=text/plain",
		"^TODO*."         => "svn:eol-style=native; svn:mime-type=text/plain"
	);

my @filesList=@ARGV;
if ($#filesList < 0)
{
	die "Usage: svnautopropsbycmd file1 file2 ...\n\tNOTE: file also means folder - in which case it will be scanned recursively\n";
}

# go until exhausted all files
while (@filesList)
{
	my $filePath=File::Spec->rel2abs(shift(@filesList));
	if (-f $filePath)
	{
		my $fileProps=checkFileSettings($filePath, \%bySuffixAutoPropsSettings, \%byBaseNameAutoPropsSettings);
		if ($fileProps ne "")
		{
			svnPropSet($filePath, $fileProps);
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
			my $fileProps=checkFileSettings($subFilePath, \%bySuffixAutoPropsSettings, \%byBaseNameAutoPropsSettings);
			if ($fileProps ne "")
			{
				$subFilesSettings{ $subFilePath } = $fileProps;
			}
		}
	}
	closedir($DIR);

	while(my($subFilePath, $fileProps)=each(%subFilesSettings))
	{
		svnPropSet($subFilePath, $fileProps);
	} 
}
