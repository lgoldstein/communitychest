#!/usr/bin/perl
# This script dumps all the registered Win32 MIME types

use File::Spec;
use Win32::Registry;

##############################################################################

$OPT_FORMAT="--format";
	$FMT_UTILPROPS="util:properties";
	$FMT_UTILMAP="util:map";
	$FMT_CSV="csv";
	$FMT_RAW="raw";
	$DEFAULT_FORMAT=$FMT_RAW;
$OPT_TITLES="--titles";
	$TTL_ON="on";
	$TTL_FF="off";
	$DEFAULT_TITLES=$TTL_ON;
$OPT_FILE="--file";
$OPT_HELP="--help";

##############################################################################

sub dieWithMessage {
	my($code,$msg)=@_;
	print STDERR "ERROR $code: $msg\n";
	exit $code;
}

sub showUsage {
	print "Usage: win32mimetypes [OPTIONS]\n";
	print "\n";
	print "Where OPTIONS are: \n";
	print "\n";
		print "\t$OPT_FORMAT=$FMT_UTILPROPS/$FMT_UTILMAP/$FMT_CSV/$FMT_RAW - output format (default=$DEFAULT_FORMAT)\n";
		print "\t$OPT_TITLES=$TTL_ON/$TTL_OFF - show titles for $FMT_CSV, $FMT_RAW formats (default=$DEFAULT_TITLES)\n";
		print "\t$OPT_FILE=<file path> - output file (default=STDOUT)\n";
		print "\t$OPT_HELP - show this help message\n";
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
		$usedOptionsRef->{$optName} = $optVal;	# mark the option as used
	}

	return $usedOptionsRef;
}

# ---------------------------------------------------------------------------- 

sub parseCommandLineOptions {
	my ($usedOptionsRef,$cmdArgsRef)=@_;
	my @cmdArgs=@{ $cmdArgsRef };

	while (@cmdArgs)
	{
		my	$optSpec=shift(@cmdArgs);
		# if found help request then process no more and die after the usage message
		if ($optSpec eq $OPT_HELP)
		{
			showUsage();
			die;
		}

		my	$argIndex=index($optSpec, '=');
		if (($argIndex < 0) && (argIndex == (length($optSpect) - 1)))
		{
			dieWithMessage(333, "No argument specified for ".$optSpec);
		}

		my	$optName=substr($optSpec, 0, $argIndex);
		my	$optVal=substr($optSpec, $argIndex + 1);
		if (($optName eq $OPT_FORMAT)
		 || ($optName eq $OPT_TITLES)
		 || ($optName eq $OPT_FILE))
		{
			updateOptionValue($optName, undef, $optVal, $usedOptionsRef);
		}
	}
	
	updateDefaultOption($OPT_FORMAT, $DEFAULT_FORMAT, $usedOptionsRef);
	updateDefaultOption($OPT_TITLES, $DEFAULT_TITLES, $usedOptionsRef);
}

##############################################################################

sub writeTitles {
	my($fileHandle,$usedOptionsRef)=@_;
	my	$titlesValue=$usedOptionsRef->{ $OPT_TITLES };
	if ($titlesValue eq $TTL_ON)
	{
		print $fileHandle "Suffix,MIME-Type\n";
		return 1;
	}

	return 0;
}

sub dumpRawTypes {
	my($fileHandle,$usedOptionsRef,$typesMapRef)=@_;

	writeTitles($fileHandle,$usedOptionsRef);

	foreach my $typeName (sort(keys(%$typesMapRef)))
	{
		my	$typeValue=$typesMapRef->{ $typeName };
		print $fileHandle $typeName."\t".$typeValue."\n";
	} 
}

sub dumpCsvTypes {
	my($fileHandle,$usedOptionsRef,$typesMapRef)=@_;

	writeTitles($fileHandle,$usedOptionsRef);

	foreach my $typeName (sort(keys(%$typesMapRef)))
	{
		my	$typeValue=$typesMapRef->{ $typeName };
		print $fileHandle "\'".$typeName."\',\'".$typeValue."\'\n";
	} 
}

sub dumpUtilMapTypes {
	my($fileHandle,$usedOptionsRef,$typesMapRef)=@_;

	print $fileHandle "\t<util:map id=\"mimeTypesMap\" map-class=\"java.util.TreeMap\" key-type=\"java.lang.String\" value-type=\"java.lang.String\" scope=\"singleton\">\n";

	foreach my $typeName (sort(keys(%$typesMapRef)))
	{
		my	$typeValue=$typesMapRef->{ $typeName };
		print $fileHandle "\t\t<entry key=\"$typeName\" value=\"$typeValue\" />\n";
	}
	
	print $fileHandle "\t</util:map>\n"; 
}

sub dumpUtilPropsTypes {
	my($fileHandle,$usedOptionsRef,$typesMapRef)=@_;

	print $fileHandle "\t<util:properties id=\"mimeTypesProperties\" scope=\"singleton\">\n";

	foreach my $typeName (sort(keys(%$typesMapRef)))
	{
		my	$typeValue=$typesMapRef->{ $typeName };
		print $fileHandle "\t\t<prop key=\"$typeName\">$typeValue</prop>\n";
	}
	
	print $fileHandle "\t</util:properties>\n"; 
}

sub writeMimeTypes {
	my($fileHandle,$usedOptionsRef,$typesMapRef)=@_;
	my	$outputFormat=$usedOptionsRef->{ $OPT_FORMAT };
	if ($FMT_RAW eq $outputFormat)
	{
		dumpRawTypes($fileHandle,$usedOptionsRef,$typesMapRef);
	}
	elsif ($FMT_CSV eq $outputFormat)
	{
		dumpCsvTypes($fileHandle,$usedOptionsRef,$typesMapRef);
	}
	elsif ($FMT_UTILMAP eq $outputFormat)
	{
		dumpUtilMapTypes($fileHandle,$usedOptionsRef,$typesMapRef);
	}
	elsif ($FMT_UTILPROPS eq $outputFormat)
	{
		dumpUtilPropsTypes($fileHandle,$usedOptionsRef,$typesMapRef);
	}
	else
	{
		dieWithMessage(2, "Unknown format type: $outputFormat");
	}
}

sub dumpMimeTypes {
	my($usedOptionsRef,$typesMapRef)=@_;
	if (exists($usedOptionsRef->{ $OPT_FILE }))
	{
		my	$orgPath=$usedOptionsRef->{ $OPT_FILE };
		my	$outPath=File::Spec->rel2abs($orgPath);
		open my $FILEHANDLE, ">", $outPath or die "Failed to open output file: ".$!;
		writeMimeTypes(\*FILEHANDLE,$usedOptionsRef,$typesMapRef);
		close $FILEHANDLE;
	}
	else
	{
		writeMimeTypes(\*STDOUT,$usedOptionsRef,$typesMapRef);
	}
}

###################################### MAIN ##################################

my %usedOptions=();
parseCommandLineOptions(\%usedOptions, \@ARGV);

my $hKey=undef;
my @keyList=();
my $rootLocation="Software\\Classes";
$HKEY_LOCAL_MACHINE->Open($rootLocation, $hKey)|| die "Failed to open $rootLocation location: ".$!;
$hKey->GetKeys(\@keyList);
$hKey->Close();

if ($#keyList < 0)
{
	dieWithMessage(1, "No registered classes extracted");
}

my	%typesMap=();
my $TYPE_VALUE="Content Type";
foreach my $keyName (@keyList)
{
	if ($keyName !~ /^\.[a-zA-Z0-9]+$/)
	{
		next;
	}

	my	$keyPath=$rootLocation."\\".$keyName;
	my	%values;
	$HKEY_LOCAL_MACHINE->Open($keyPath, $hKey)|| die "Failed to open $keyPath location: ".$!;
	$hKey->GetValues(\%values);
	$hKey->Close();

	foreach my $value (keys(%values))
	{
		my	$RegKey 	= $values{$value}->[0];
		my	$RegType 	= $values{$value}->[1];
		my	$RegValue 	= $values{$value}->[2];
		next if ($RegKey eq '');	# skip the default key
		next if ($RegType ne '1');	# skip non-REG_SZ values
		next if ($RegKey ne $TYPE_VALUE);

		if (exists($typesMap{$keyName}))
		{
			dieWithMessage(4, "Multiple mappings for $keyName");
		}
		
		$typesMap{$keyName} = $RegValue;
	}
}

dumpMimeTypes(\%usedOptions,\%typesMap);