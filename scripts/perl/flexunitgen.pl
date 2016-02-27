#!/usr/bin/perl

use Cwd;
use File::Spec;
use File::stat;

# ---------------------------------------------------------------------------- 

sub showUsage {
	print "Usage: flexunitgen [OPTIONS]\n";
	print "\n";
	print "Where OPTIONS are: \n";
	print "\n";
	print "\t-source <folder> - default=CWD\n";
	print "\t-target <folder> - default=same as source folder\n";	
	print "\t-package <package-name> - default=empty\n";	
	print "\t-class <class-name> - default=AllTests\n";	
	print "\t-main [actionscript | junit | ui] - generate as ActionScript or junit/UI MXML (default=actionscript)\n";
	print "\t-force - re-generate even if nothing changed\n";
}

# ---------------------------------------------------------------------------- 

sub dieWithUsage {
	my($code,$msg)=@_;
	print STDERR "ERROR $code: $msg\n\n";
	showUsage();
	exit $code;
}

# ----------------------------------------------------------------------------

sub checkOptionValue {
	my($optName,$optVal,$curVal)=@_;
	if (defined $curVal)
	{
		dieWithUsage(200, "Option $optName re-specified - previous=$curVal/new=$optVal");
	}

	return $optVal;
}

# ----------------------------------------------------------------------------

sub resolveOptionValue {
	my($optName,$optVal,$curVal)=@_;
	if (defined $curVal)
	{
		return $curVal;
	}

	return $optVal;
}

# ----------------------------------------------------------------------------

sub getTestsList {
	my ($srcFolder, $rootPkg, $lastModTime, $forced)=@_;
	my	@testsList=();
	my	@subDirs=();

	# read all sub-folders into a list to avoid having too many open DIR(s) due to recursion depth
	opendir(my $DIR, $srcFolder) || die "Error in opening dir $srcFolder: ".$!;
	while((my $fileName=readdir($DIR)))
	{
		# TODO use a @ignoredPatterns list
		if (($fileName eq ".")			# skip current entry
		 || ($fileName eq ".."))		# skip parent entry
	 	{
			next;
		}

		# skip hidden files/directories (e.g. SVN)
		if ($fileName =~ m/^\..*/)
		{
			next;
		}

		my	$subFilePath=File::Spec->catfile($srcFolder, $fileName);
		# push sub-directory for further scan
		if (-d $subFilePath)
		{
			push(@subDirs, $fileName);
			next;
		}

		if ($fileName !~ m/.*Test\.as$/)
		{
			next;
		}

		my	$testName=substr($fileName, 0, length($fileName) - 3);
		if (defined $rootPkg)
		{
			push(@testsList, $rootPkg . "." . $testName);
		}
		else
		{
			push(@testsList, $testName);
		}

		# if not forced and have a last-modified time check if need to re-generated 
		if ((! defined $forced) && (defined $lastModTime))
		{
			my	$subModified=stat($subFilePath)->mtime;
			if ($subModified < $lastModTime)
			{
				next;
			}		
		}

		if (! defined $forced)
		{
			$forced = 1;
		}
	}

	closedir($DIR);

	while (@subDirs)
	{
		my	$subName=shift(@subDirs);
		my	$subPath=File::Spec->catfile($srcFolder, $subName);
		my	$subPkg;
		if (defined $rootPkg)
		{
			$subPkg = $rootPkg . "." . $subName;
		}
		else
		{
			$subPkg = $subName;
		}

		my($subForce,@subTests)=getTestsList($subPath, $subPkg, $lastModTime, $forced);
		if (!defined $forced)
		{
			$forced = $subForce;
		}

		if ($#subTests >= 0)
		{
			push(@testsList,@subTests);
		}
	}

	return ($forced, @testsList);
}

# ----------------------------------------------------------------------------

sub generateImportedTests {
	my($RESULT,@testsList)=@_;

	print $RESULT "\timport flexunit.framework.TestSuite;\n\n";

	foreach my $testName (@testsList)
	{
		print $RESULT "\timport $testName;\n";
	}
}

# ----------------------------------------------------------------------------

sub generateSuiteFunction {
	my($RESULT,$accessType,@testsList)=@_;
	
	print $RESULT "\t\t$accessType static function suite():TestSuite {\n";
	print $RESULT "\t\t\tvar ts:TestSuite = new TestSuite();\n";
	foreach my $testPath (@testsList)
	{
		my	$dotPos=rindex($testPath, '.');
		my	$testName=undef;
		if ($dotPos > 0)
		{
			$testName = substr($testPath, $dotPos + 1, length($testPath) - $dotPos - 1);
		}
		else
		{
			$testName = $testPath;
		}

		print $RESULT "\t\t\tts.addTestSuite($testName);\n";
	}
	print $RESULT "\t\t\treturn ts;\n";
	print $RESULT "\t\t}\n";
}

# ----------------------------------------------------------------------------

sub generateTestsMXML {
	my ($classPath,$pkgName,$clsName,$mainType,@testsList)=@_;

	print "Generating $classPath\n";

	open my $RESULT, ">$classPath" || die "Error in creating result file $classPath: ".$!;
	print $RESULT "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
	print $RESULT "<mx:Application\n"; 
	print $RESULT "\txmlns:mx=\"http://www.adobe.com/2006/mxml\"\n";
    print $RESULT "\txmlns:flexunit=\"flexunit.flexui.*\"\n"; 
	print $RESULT "\tcreationComplete=\"onCreationComplete()\"\n";
	print $RESULT "\t>\n";    

	print $RESULT "\n\t<mx:Script>\n";
	print $RESULT "\t\t<![CDATA[\n";

	generateImportedTests($RESULT,@testsList);
	print $RESULT "\n";
	generateSuiteFunction($RESULT, "private", @testsList);
	print $RESULT "\n";

    if ($mainType eq "junit")
    {
		print $RESULT "\n";
		print $RESULT "\t\tprivate function onTestsComplete () : void {	fscommand(\"quit\"); }\n";
		print $RESULT "\n";
    }

    print $RESULT "\t\tprivate function onCreationComplete():void {\n";
    if ($mainType eq "junit")
    {
		print $RESULT "\t\t\tvar tests:TestSuite=suite();\n";
		print $RESULT "\t\t\tvar antRunner:JUnitTestRunner=new JUnitTestRunner();\n";
		print $RESULT "\t\t\tantRunner.run(tests, onTestsComplete);\n";
    }
    else
    {
		print $RESULT "\t\t\ttestRunner.test = suite();\n";
		print $RESULT "\t\t\ttestRunner.startTest();\n";
    }
	print $RESULT "\t\t}\n";

	print $RESULT "\t\t]]>\n";
	print $RESULT "\t</mx:Script>\n";
    if ($mainType eq "ui")
	{    
    	print $RESULT "\n\t<flexunit:TestRunnerBase id=\"testRunner\" width=\"100%\" height=\"100%\" />\n";
	}
	print $RESULT "</mx:Application>\n";
	
	close $RESULT;
}

# ----------------------------------------------------------------------------

sub generateTestsClass {
	my ($classPath,$pkgName,$clsName, @testsList)=@_;

	print "Generating $classPath\n";

	open my $RESULT, ">$classPath" || die "Error in creating result file $classPath: ".$!;
	if (defined $pkgName)
	{
		print $RESULT "package $pkgName {\n";
	}
	else
	{
		print $RESULT "package {\n";
	}

	generateImportedTests($RESULT, @testsList);

	print $RESULT "\n\tpublic class $clsName {\n\n";

	generateSuiteFunction($RESULT, "public", @testsList);

	print $RESULT "\t}\n";
	print $RESULT "}\n";
	close $RESULT;
}

# ----------------------------------------------------------------------------

sub generateTestSuite {
	my ($srcFolder,$tgtFolder,$pkgName,$clsName,$forced,$mainType)=@_;
	my ($classFolder,$pkgPath)=(undef,$pkgName);
	if (defined $pkgPath)
	{
		if ($^O =~ m/.*Win32.*/)
		{
			$pkgPath =~ s/\./\\/g;
		}
		else
		{
			$pkgPath =~ s/\./\//g;
		}
		
		$classFolder = File::Spec->catfile($tgtFolder, $pkgPath);
	}
	else
	{
		$classFolder = $tgtFolder;
	}

	my	$classPath=undef;
	if (($mainType eq "junit") || ($mainType eq "ui"))
	{
		$classPath = File::Spec->catfile($classFolder, $clsName . ".mxml"); 
	}
	else
	{
		$classPath = File::Spec->catfile($classFolder, $clsName . ".as"); 
	}

	my	$lastModTime=undef;
	if (-e $classPath)
	{
		$lastModTime = stat($classPath)->mtime;
	}

	my	($genTests,@testsList)=getTestsList($srcFolder, undef, $lastModTime, $forced);
	if ($#testsList < 0)
	{
		if (defined $genTests)
		{
			print "No tests to generate";
		}
		else
		{
			print "All tests up-to-date";
		}

		return;
	}
	else
	{
		if (!defined $genTests)
		{
			print "All tests up-to-date";
			return;
		}
	}

#	print "Generate tests from $srcFolder to $classPath\n";
	if (($mainType eq "junit") || ($mainType eq "ui"))
	{
		generateTestsMXML($classPath, $pkgName, $clsName, $mainType, @testsList);
	}
	else
	{
		generateTestsClass($classPath, $pkgName, $clsName, @testsList);
	}
}

# ----------------------------------------------------------------------------

my ($srcFolder,$tgtFolder,$pkgName,$clsName,$forced,$mainType)=undef;

my @argsList=@ARGV;
while(@argsList)
{
	my	$optName=shift(@argsList);
	if ($optName eq "-help")
	{
		showUsage();
		exit 1;
	}

	if ($optName eq "-force")
	{
		$forced = checkOptionValue($optName, 1, $forced);
		next;
	}

	if ($#argsList < 0)
	{
		dieWithUsage(100, "Missing argument for $optName option");
	}

	my	$optVal=shift(@argsList);
	if ($optName eq "-source")
	{
		$srcFolder = checkOptionValue($optName, $optVal, $srcFolder);
		next;
	}

	if ($optName eq "-target")
	{
		$tgtFolder = checkOptionValue($optName, $optVal, $tgtFolder);
		next;
	}

	if ($optName eq "-package")
	{
		$pkgName = checkOptionValue($optName, $optVal, $pkgName);
		next;
	}

	if ($optName eq "-class")
	{
		$clsName = checkOptionValue($optName, $optVal, $clsName);
		next;
	}

	if ($optName eq "-main")
	{
		$mainType = checkOptionValue($optName, $optVal, $mainType);
		if (($mainType eq "actionscript")
		 || ($mainType eq "junit")
		 || ($mainType eq "ui"))
		{
			next;
		}
		else
		{
			dieWithUsage(101, "Unknown/Unsupported $optName option: $optVal");
		}
	}
}

$srcFolder = resolveOptionValue("-source", getcwd, $srcFolder);
$tgtFolder = resolveOptionValue("-target", $srcFolder, $tgtFolder);
$clsName = resolveOptionValue("-class", "AllTests", $clsName);
$mainType = resolveOptionValue("-main", "actionscript", $mainType);

generateTestSuite($srcFolder,$tgtFolder,$pkgName,$clsName,$forced,$mainType);
