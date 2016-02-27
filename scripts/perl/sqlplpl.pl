#!/usr/bin/perl

use Sys::Hostname;
use Socket;

# ---------------------------------------------------------------------------- 

sub showUsage {
	print "Usage: sqlplpl [OPTIONS] extra-args\n";
	print "\n";
	print "Where OPTIONS are: \n";
	print "\n";
	print "\t-host <host name/address> - default=local host name\n";
	print "\t-port <port> - default=1521\n";	
	print "\t-user <username>\n";
	print "\t-pass <password>\n";
	print "\t-login <username>/<password>\n";
	print "\t-role <role> - add 'as <role>' to the login command\n";
	print "\t-sid SID to connect to - default=ORACLE_SID environment variable value\n";
	print "\t-invarg <value> - add the argument to the SQLPLUS invocation options (before the login)\n";
	print "\t-help show this help message\n";
	print "\n";
	print "NOTE(s):\n";
	print "\n";
	print "\t1. The 1st command line argument that does not start with a '-' is considered the 1st extra argument\n";
}

# ---------------------------------------------------------------------------- 

sub dieWithUsage {
	my($code,$msg)=@_;
	print STDERR "ERROR $code: $msg\n\n";
	showUsage();
	exit $code;
}

# ---------------------------------------------------------------------------- 

sub resolveLocalHost {
	if (exists $ENV{'ORACLE_HOST'})
	{
		return $ENV{'ORACLE_HOST'};
	}

	my	$dbHost=hostname();
	my	@hostInfo=gethostbyname($dbHost);
	if ($#hostInfo >= 4)
	{
		my	@ipVal=unpack("C4",$hostInfo[4]);
		return join(".", @ipVal);
	}

	if (defined $dbHost)
	{
		return $dbHost;
	}

	return "localhost";
}

##################################### MAIN ###################################

my @cmdArgs=@ARGV;
if ($#cmdArgs < 0)
{
	showUsage();
	exit;
}

# assume local host by default
my $dbHost=resolveLocalHost();

# default Oracle port
my $dbPort=1521;
# assume initial parameters undefined
my($dbSid,$dbUser,$dbPass,$dbRole)=(undef,undef,undef,undef);
# check if have a default SID set
if (exists $ENV{'ORACLE_SID'})
{
	$dbSid=$ENV{'ORACLE_SID'};
}

# loop over the command line arguments till first non-option value
my %usedOptions=();
my @invokeOpts=();
while (@cmdArgs)
{
	my	$optName=shift(@cmdArgs);
	if ($optName =~ m/^-.*/)
	{
		if ($#cmdArgs < 0)
		{
			dieWithUsage(900, "Missing $optName argument");
		}

		# check if option re-specified
		if (exists $usedOptions{$optName})
		{
			dieWithUsage(901, "Option $optName re-specified");
		}

		my $optVal=shift(@cmdArgs);
		# we allow re-usage of the "-invarg" option
		if ($optName eq "-invarg")
		{
			push(@invokeOpts, $optVal);
			next;
		}

		# mark the option
		$usedOptions{$optName} = $optVal;
		if ($optName eq "-user")
		{
			$dbUser = $optVal;
			next;
		}

		if ($optName eq "-pass")
		{
			$dbPass = $optVal;
			next;	
		}

		if ($optName eq "-host")
		{
			$dbHost = $optVal;
			next;	
		}

		if ($optName eq "-port")
		{
			$dbPort = $optVal;
			next;	
		}

		if ($optName eq "-sid")
		{
			$dbSid = $optVal;
			next;	
		}

		if ($optName eq "-login")
		{
			my @vList=split(/\//, $optVal);
			if ($#vList != 1)
			{
				dieWithUsage(902, "Bad value for $optName option");				
			}

			($dbUser,$dbPass)=@vList;
			next;
		}

		if ($optName eq "-role")
		{
			$dbRole = $optVal;
			next;
		}

		print STDERR "Unknown option: $optName\n";
		showUsage();
		die;
	}
	else
	{
		# return the shifted value to the command line extra arguments
		unshift(@cmdArgs, $optName);
		last;
	}
}

if (!defined $dbUser)
{
	dieWithUsage(903, "Missing login user");
}

if (!defined $dbPass)
{
	dieWithUsage(904, "Missing login password");
}

if (!defined $dbSid)
{
	dieWithUsage(905, "Missing SID");
}

if (!defined $dbHost)
{
	dieWithUsage(906, "Missing host");
}

# start building the command line 
my $execBase="sqlplus";
# append the invocation arguments
foreach my $invVal (@invokeOpts)
{
	$execBase = $execBase . " " . $invVal;
}

# append the login data and connection URL
$execBase = $execBase . " " . "$dbUser/$dbPass\@'(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(Host=$dbHost)(Port=$dbPort))(CONNECT_DATA=(SID=$dbSid)))'";

# append the extra role (if defined)
if (defined $dbRole)
{
	$execBase = $execBase . " as " . $dbRole;
}

# append extra command line arguments (if any)
foreach my $cmdVal (@cmdArgs)
{
	$execBase = $execBase . " " . $cmdVal;
}

my $execCmd=undef;
if ($^O =~ m/.*Win32.*/)
{
	$execCmd = "start $execBase";
}
else
{
	$execCmd = $execBase;
}

#print "$execCmd\n";
exec($execCmd) || die "Failed to execute: " . $!;
