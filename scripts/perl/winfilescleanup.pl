#!/usr/bin/perl
# Removes all sort of hidden Windows files

use File::Spec;
use File::Basename;
use File::Remove;

sub trim {
	my($string)=@_;
	$string =~ s/^\s+//;
	$string =~ s/\s+$//;
	return $string;
}

sub dieWithMessage {
	my($code,$msg)=@_;
	print STDERR "ERROR $code: $msg\n\n";
	exit $code;
}

# params: file name, associations hashes references
# returns the required properties if any match found - null/empty otherwise
sub checkFileSettings {
	my($fileName,$sfxPropsRef,$namePropsRef)=@_;
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

	return "";
}

sub scanFolder {
	my($folderPath,$sfxPropsRef,$namePropsRef)=@_;
	
	if (! -d $folderPath)
	{
		dieWithMessage(2, "Scan target not a folder: $folderPath");
	}

	print "\t$folderPath\n";

	my @foldersList=();
	my %filesList=();
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
			my	$checkResult=checkFileSettings($fileName,$sfxPropsRef,$namePropsRef);
			if ($checkResult ne "")
			{
				$filesList{ $fileName } = $checkResult;
			}
			else
			{
				print "\t\tSkip $fileName\n";
			}
		}
	}

	while(my($fileKey,$reasonPhrase)=each(%filesList))
	{
		my	$subFilePath=File::Spec->catfile($folderPath, $fileKey);
		print "\t\tDelete $fileKey: $reasonPhrase\n";
		File::Remove::remove($subFilePath);
	}

	return @foldersList;
}

###################################### MAIN ##################################

my %bySuffixSettings=(
		".*\.ini\$"        => "ini file"
	);
my %byNameSettings=(	
		# Well-known used file names
		"^\\..*"    	=> "hidden file",
		"^\\~.*"        => "temporary file",
		"^Thumbs.db\$"	=> "thumbs file",
		"^Folder\.jpg"	=> "folder jpg file",
		"^desktop\.ini"	=> "desktop ini file",
		"^AlbumArt.*\$" => "album art file"
	);

my @filesList=@ARGV;
if ($#filesList < 0)
{
	dieWithMessage(1, "Usage: winfilescleanup folder1 folder2 ...\n");
}

# go until exhausted all files
while (@filesList)
{
	my	$orgPath=shift(@filesList);
	my	$resPath=File::Spec->rel2abs($orgPath);
	my	@subFolders=scanFolder($resPath,\%bySuffixSettings,\%byNameSettings);
	if ($#subFolders >= 0)
	{
		push(@filesList,@subFolders);
	}
}
