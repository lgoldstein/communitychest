#!/usr/bin/perl
# This script scans a give folder for files having some kind of common prefix,
# suffix + running index and renames them to use a fixed width number - according to
# the nunber of digits of the highest index

use File::Spec;
use File::Copy;
use File::Basename;
use File::Path;
use Cwd;
use List::Util;

##############################################################################

sub dieWithMessage {
	my($code,$msg)=@_;
	print STDERR "ERROR $code: $msg\n";
	exit $code;
}

sub calculateLongestPrefix {
	my($longestMatch,$fileName)=@_;

	if (!defined($longestMatch))
	{
		return $fileName;
	}

	# if file name already starts with longest match then do nothing
	if ($fileName =~ m/^$longestMatch/)
	{
		return $longestMatch;	
	}

	my($matchLen,$nameLen)=(length($longestMatch),length($fileName));
	my	$cmpLen=List::Util::min($matchLen,$nameLen);
	my	@matchChars=unpack("C*", $longestMatch);
	my	@nameChars=unpack("C*", $fileName);
	for (my $curPos=0; $curPos < $cmpLen; $curPos++)
	{
		my	$chMatch=$matchChars[$curPos];
		my	$chName=$nameChars[$curPos];
		if ($chMatch ne $chName)
		{
			if ($curPos <= 0)
			{
				dieWithMessage(8, "No common prefix [$longestMatch] with $fileName");
			}
			
			return substr $longestMatch, 0, $curPos;
		}
	}

	# This point is reached if all characters match - which should have been caught by the prefix match
	dieWithMessage(9, "Common prefix [$longestMatch] internal error for $fileName");
}

sub calculateFileSuffix {
	my($fileSuffix,$fileName,$subFilePath)=@_;
	my($baseName,$dirPath,$fileType)=fileparse($subFilePath, qr/\.[^.]*/);
	if (!defined($fileType))
	{
		dieWithMessage(6, "Cannot determine file suffix of $fileName");
	}

	if ((defined($fileSuffix)) && ($fileSuffix ne $fileType))
	{
		dieWithMessage(7, "Mismatched file type [$fileType]: $fileName");
	}

	return $fileType;
}

sub extractFileIndex {
	my($longestMatch,$fileSuffix,$fileName)=@_;
	my($matchLen,$suffixLen,$nameLen)=(length($longestMatch),length($fileSuffix),length($fileName));
	return substr $fileName, $matchLen, ($nameLen - $matchLen - $suffixLen);
}

sub calculateMaxIndex {
	my($longestMatch, $fileSuffix, @filesList)=@_;
	my $maxIndex=(-1);

	foreach my $fileName (@filesList)
	{
		my	$strIndex=extractFileIndex($longestMatch,$fileSuffix,$fileName);
		my	$fileIndex=int($strIndex);
		if ($fileIndex > $maxIndex)
		{
			$maxIndex = $fileIndex;
		}
	}

	if ($maxIndex <= 0)
	{
		die(8, "Cannot determine max. index of files");
	}

	return "$maxIndex";
}

sub renameFiles {
	my($folderPath,$longestMatch,$fileSuffix,@filesList)=@_;
	my $maxIndex=calculateMaxIndex($longestMatch, $fileSuffix, @filesList);
	my $idxLen=length($maxIndex);

	foreach my $fileName (@filesList)
	{
		my	$fileIndex=extractFileIndex($longestMatch,$fileSuffix,$fileName);
		while(length($fileIndex) < $idxLen)
		{
			$fileIndex = "0".$fileIndex;
		}
		
		my	$newName=$longestMatch.$fileIndex.$fileSuffix;
		# check if rename yields a new name
		if ($newName eq $fileName)
		{
			next;
		}

		print "\t\t$fileName => $newName\n";

		my	$oldPath=File::Spec->catfile($folderPath, $fileName);
		my	$newPath=File::Spec->catfile($folderPath, $newName);
		File::Copy::move($oldPath, $newPath);
	}
}

sub scanFolder {
	my($folderPath)=@_;
	if (! -d $folderPath)
	{
		dieWithMessage(2, "Not a folder: $folderPath");		
	}
	
	print "\t$folderPath\n";

	my @foldersList=();
	my @filesList=();
	my ($longestMatch,$fileSuffix)=(undef,undef);

	opendir(my $DIR, $folderPath) || die "Error in opening dir $folderPath: ".$!;
	while((my $fileName=readdir($DIR)))
	{
		if (($fileName eq ".") || ($fileName eq ".."))
		{
			next;
		}

		my	$subFilePath=File::Spec->catfile($folderPath, $fileName);
		if (-d $subFilePath)	# we care only about folders
		{
			push(@foldersList, $subFilePath);
		}
		else
		{
			$longestMatch = calculateLongestPrefix($longestMatch, $fileName);
			$fileSuffix = calculateFileSuffix($fileSuffix,$fileName,$subFilePath);
			push(@filesList, $fileName);
		}		
	}
	closedir($DIR);

	if ($#filesList > 0)
	{
		if ($#foldersList >= 0)
		{
			dieWithMessage(3, "Mixed contents in $folderPath");
		}

		renameFiles($folderPath, $longestMatch, $fileSuffix, @filesList);
	}

	return @foldersList;
}

###################################### MAIN ##################################

my @filesList=@ARGV;
if ($#filesList < 0)
{
	dieWithMessage(1, "Usage: seqfilenumbers folder1 folder2 ...");
}

# go until exhausted all files
while (@filesList)
{
	my	$orgPath=shift(@filesList);
	my	$resPath=File::Spec->rel2abs($orgPath);
	my	@subFolders=scanFolder($resPath);
	if ($#subFolders >= 0)
	{
		push(@filesList,@subFolders);
	}
}
