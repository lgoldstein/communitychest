#!/usr/bin/perl

use File::Spec;
use File::Basename;
use Cwd;

sub takeFromEnvironment {
	my($curVal,$varName)=@_;

	if (!defined($curVal))
	{
		if (exists($ENV{$varName}))
		{
			$curVal = $ENV{$varName};
		}
		else
		{
			die "No ".$varName." definition found";
		}
	}

#	print $varName."=".$curVal."\n";
	return $curVal;
}

# finds ALL JAR(s) in the specified root folder - including sub-folders
# returns a list of their RELATIVE path(s) from the root
sub getSubFolderJars {
	my($rootFolder)=@_;
	my @filesList=($rootFolder);
	my @jarFiles=();

	if (! -e $rootFolder)
	{
		return @jarFiles;
	}

	# go until exhausted all files
	while (@filesList)
	{
		my $filePath=shift(@filesList);
		if (! -d $filePath)
		{
			next;
		}

		# read all sub-folders into a list to avoid having too many open DIR(s) due to recursion depth
		opendir(my $DIR, $filePath) || die "Error in opening dir $filePath: ".$!;
		while((my $fileName=readdir($DIR)))
		{
			if (($fileName eq ".")			# skip current entry
			 || ($fileName eq ".."))			# skip parent entry
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
				my($baseName,$dirPath,$fileType)=fileparse($subFilePath, qr/\.[^.]*/);
				if ($fileType eq ".jar")
				{
					push(@jarFiles, $subFilePath);
				}
			}
		}
		closedir($DIR);
	}
	
	return @jarFiles;
}

sub executeApplication {
	my($appHome,$javaHome,$antHome,@extraArgs)=@_;
	my @extraJarsFolders=(
			File::Spec->catfile($appHome, 'bin'),
			File::Spec->catfile($appHome, 'lib'),
			File::Spec->catfile($antHome, 'lib')
		);
	my @extraClasspathJars=();
	for my $jarsSubFolder (@extraJarsFolders)
	{
		my	@subFolderJars=getSubFolderJars($jarsSubFolder);
		if (@subFolderJars)
		{
			push(@extraClasspathJars, @subFolderJars);
		}
	}

	my($pathSeparator,$javaCmd)=();
	if ($^O =~ m/.*Win32.*/)
	{
		$pathSeparator = ';';
		$javaCmd = "javaw.exe";
	}
	else
	{
		$pathSeparator = ':';
		$javaCmd = "java";
	}

	my	$classPath=shift(@extraClasspathJars);
	while(@extraClasspathJars)
	{
		my	$jarPath=shift(@extraClasspathJars);
		$classPath = $classPath . $pathSeparator . $jarPath;
	}
	if (!defined($classPath))
	{
		die "No extra classpath JAR(s) found";
	}

	my $javaBinPath=File::Spec->catfile($javaHome, 'bin');
	my $javaExecPath=File::Spec->catfile($javaBinPath, $javaCmd);
	if (! -e $javaExecPath)
	{
		die $javaExecPath . " file not found";
	}

	my $execCmd=undef;
	if ($^O =~ m/.*Win32.*/)
	{
		$execCmd = "start /D\"$appHome\" \"$javaExecPath\" \"-Dant.home=$antHome\" -classpath \"$classPath\" net.community.apps.apache.ant.antrunner.Main @extraArgs";
	}
	else
	{
		$execCmd = "$javaExecPath -Dant.home=$antHome -classpath $classPath net.community.apps.apache.ant.antrunner.Main @extraArgs";
	}

	exec($execCmd) || die "Failed to execute: " . $!;
}

my $javaHome=undef;
my $antHome=undef;
my(undef,$appHome,undef)=fileparse($0, qr/\.[^.]*/);
my @orgArgs=@ARGV;
my @invArgs=();

while (@orgArgs)
{
	my $argName=shift(@orgArgs);
	if ($argName eq "-vm")
	{
		$javaHome = shift(@orgArgs);
		next;
	}

	if ($argName eq "-anthome")
	{
		$antHome = shift(@orgArgs);
		next;
	}

	if ($argName eq "-apphome")
	{
		$appHome = shift(@orgArgs);
		next;
	}

	push(@invArgs, $argName);
}

$javaHome = takeFromEnvironment($javaHome, 'JAVA_HOME');
$antHome = takeFromEnvironment($antHome, 'ANT_HOME');
executeApplication($appHome,$javaHome,$antHome,@invArgs);
