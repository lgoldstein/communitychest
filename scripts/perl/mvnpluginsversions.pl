#!/usr/bin/perl
#
# Scans a POM file and checks if the defined / used plugins have a better version
#

use File::Spec;
use File::Basename;
use File::Path;
use Cwd;

$MVN_PROJECT="project";
$MVN_PROPERTIES="properties";
$MVN_PLUGIN="plugin";
$MVN_GROUPID="groupId";
$MVN_ARTIFACTID="artifactId";
$MVN_VERSION="version";
$MVN_PLUGIN_REPO="pluginRepository";

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

sub getEnvVarValue {
    my($envVarName)=@_;
    if (exists($ENV{$envVarName}))
    {
        return $ENV{$envVarName};
    }

    return undef;
}

##################################### Functions ################################### 

$OPT_URL_ADD="--url-add";
$OPT_URL_DEFAULT="--url-use-default";
    $DEFAULT_REPO_URL="http://repo1.maven.org/maven2";
$OPT_URL_IGNORE="--url-ignore-file";
$OPT_POM_FILE="--pom-file";
    $DEFAULT_POM_FILENAME="pom.xml";
$OPT_DEFINE_PROP="--define-property";
$OPT_IGNORE_DUP_PROPS="--ignore-duplicate-properties";
$OPT_VERBOSE="--verbose";
$OPT_HELP="--help";

sub showUsage {
    print "Usage: mvnpluginsversions [OPTIONS] [POM-FILE]\n";
    print "\n";
    print "Where OPTIONS are: \n";
    print "\n";
        print "\t$OPT_URL_ADD=<url> - Plugin repository URL to add\n";
        print "\t$OPT_DEFINE_PROP-xxx=yyy - Define property 'xxx' to have value 'yyy";
        print "\t$OPT_IGNORE_DUP_PROPS - Ignore re-defined properties values (only 1st one will apply)\n";
        print "\t$OPT_URL_DEFAULT - Add default plugin repository ($DEFAULT_REPO_URL)\n";
        print "\t$OPT_URL_IGNORE - Ignore repositories defined in the POM file\n";
        print "\t$OPT_VERBOSE - Show more verbose messages";
        print "\t$OPT_HELP - show this help message\n";
    print "\n";
        print "\tIf no POM file specified then $DEFAULT_POM_FILENAME in the CWD is used\n";
}

# ---------------------------------------------------------------------------- 

sub dieWithMessage {
    my($code,$msg)=@_;
    print STDERR "ERROR $code: $msg\n\n";
    exit $code;
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
        $usedOptionsRef->{$optName} = $optVal;  # mark the option as used
    }

    return $usedOptionsRef;
}

# ---------------------------------------------------------------------------- 

sub parseCommandLineOptions {
    my($usedOptionsRef,$propsRef,$reposRef,$cmdArgsRef)=@_;
    my @cmdArgs=@{ $cmdArgsRef };

    if ($#cmdArgs < 0)
    {
        push(@cmdArgs, $OPT_HELP);
    }

    while (@cmdArgs)
    {
        my  $optSpec=shift(@cmdArgs);
        if ($optSpec !~ /^\-/)
        {
            if ($#cmdArgs >= 0)
            {
            	dieWithMessage(303, "POM file path must be last");
            }
        	updateOptionValue($OPT_POM_FILE, undef, Cwd::realpath($optSpec), $usedOptionsRef);
        	last;
        }

        # if found help request then process no more and die after the usage message
        if ($optSpec eq $OPT_HELP)
        {
            showUsage();
            die;
        }
        elsif ($optSpec eq $OPT_URL_DEFAULT)
        {
        	updateOptionValue($optSpec, undef, $DEFAULT_REPO_URL, $reposRef);
        	next;
        }
        elsif (($optSpec eq $OPT_URL_IGNORE)
            || ($optSpec eq $OPT_VERBOSE)
            || ($optSpec eq $OPT_IGNORE_DUP_PROPS))
        {
            updateOptionValue($optSpec, undef, $optSpec, $usedOptionsRef);
            next;
        }

        my  $argIndex=index($optSpec, '=');
        if (($argIndex < 0) && (argIndex == (length($optSpect) - 1)))
        {
            dieWithMessage(333, "No argument specified for ".$optSpec);
        }

        my  $optName=substr($optSpec, 0, $argIndex);
        my  $optVal=substr($optSpec, $argIndex + 1);
        if ($optName eq $OPT_URL_ADD)
        {
            updateOptionValue($optVal, undef, $optVal, $reposRef);
        }
        elsif (index($optName, $OPT_DEFINE_PROP) == 0)
        {
        	$optName = substr($optName, length($OPT_DEFINE_PROP) + 1);
            updateOptionValue($optName, undef, $optVal, $propsRef);
        }
        else
        {
            dieWithUsage(911, "Unknown argument: $optName");
        }
    }

    updateDefaultOption($OPT_POM_FILE, File::Spec->catfile(Cwd::realpath(getcwd()), $DEFAULT_POM_FILENAME), $usedOptionsRef);
}

# ----------------------------------------------------------------------------

# returns a list of the following: doc, groupId, artifactId, mainVersion [, parentVersion or undef]
sub extractPomInformationNodes {
    my($pomFilePath,$usedOptionsRef,$propsRef,$reposRef,$pluginsRef)=@_;
    
    open(my $POMFILE, $pomFilePath) || dieWithMessage(99, "Failed to open $pomFilePath: $!");
    readPOMData($pomFilePath, $POMFILE, $usedOptionsRef, $propsRef, $reposRef, $pluginsRef);
    close $POMFILE;
}

# ---------------------------------------------------------------------------- 

sub readPOMData {
	my($filePath,$fileHandle,$usedOptionsRef,$propsRef,$reposRef,$pluginsRef)=@_;
	my $verbose=exists($usedOptionsRef->{ $OPT_VERBOSE });
    my $lineNumber=0;
    my $line=undef;
    my $tagName=undef;
    my $tagValue=undef;
    my $endElement=0;

    while(1)
    {
        ($lineNumber,$line) = readNextLine($fileHandle, $lineNumber);
        if (!defined($line))
        {
            last;
        }
        
        # Check if comment start
        ($lineNumber,$line) = adjustIfCommentOrCDATA($fileHandle, $lineNumber, $line);
        if (!defined($line))
        {
            next;
        }

        if ($line =~ /^<\?/)
        {
            # TODO validate that it is a one-line instruction
            next;
        }
        elsif ($line =~ /^<project/)
        {
            # the project element may span several lines due to namespace definitions
            if (index($line, '>') < 0)
            {
               ($lineNumber,$line) = skipProjectElement($fileHandle, $lineNumber);
            }
            next;
        }
        
        ($lineNumber,$tagName,$tagValue,$endElement) = parseElementLine($filePath, $fileHandle, $lineNumber, $line);
        if ($tagName eq $MVN_PROPERTIES)
        {
            $lineNumber = populateProperties($filePath, $fileHandle, $usedOptionsRef, $lineNumber, $propsRef);
            if ($verbose)
            {
            	print "Properties:\n";
            	while(my($key,$value)=each $propsRef)
            	{
            		print "\t$key: $value\n";
            	}
            }
        }
        elsif ($tagName eq $MVN_PLUGIN)
        {
        	my $pluginId, $version;
            ($lineNumber, $pluginId, $version) = readPluginData($filePath, $fileHandle, $lineNumber, $propsRef, $pluginsRef);
            if ($verbose)
            {
            	print "Plugin $pluginId:$version\n";
            }    	
        }
        elsif ($tagName eq $MVN_PLUGIN_REPO)
        {
        	my $repoId, $url;
        	($lineNumber, $repoId, $url) = readPluginRepository($filePath, $fileHandle, $lineNumber, $propsRef, $reposRef);
        	if ($verbose)
        	{
        		print "Repository $repoId: $url\n";
        	}
        }
        else
        {
            # LOG print "\t$lineNumber: $tagName / $tagValue / $endElement\n";
        }
    }
}

# ---------------------------------------------------------------------------- 

# Return: (line number, repo-id, url)
sub readPluginRepository {
    my($filePath,$fileHandle,$lineNumber,$propsRef,$reposRef)=@_;
    my $line=undef;
    my $tagName=undef;
    my $tagValue=undef;
    my $endElement=0;
    my $id=undef;
    my $url=undef;

    while(1)
    {
        ($lineNumber,$line) = readNextLine($fileHandle, $lineNumber);
        if (!defined($line))
        {
            dieWithMessage(85, "Premature EOF while read properties");
        }

        # Check if comment start
        ($lineNumber,$line) = adjustIfCommentOrCDATA($fileHandle, $lineNumber, $line);
        if (!defined($line))
        {
            next;
        }

        if ($line =~ /^<\/pluginRepository>/)
        {
            if (!defined($url))
            {
                dieWithMessage(40, "No plugin repository url");
            }

            if (!defined($id))
            {
                dieWithMessage(40, "No plugin repository id");
            }
            
            $reposRef->{ $id } = $url;
            # LOG print "\tRepo: $id - $url\n";
            return ($lineNumber, $id, $url);
        }
        
        if (defined($id) && defined($url))
        {
        	next;  # check if have all we want
        }
        
        ($lineNumber,$tagName,$tagValue,$endElement) = parseElementLine($filePath, $fileHandle, $lineNumber, $line);
        # TODO check if already set
        # TODO ensure tag value defined and not empty
        if ($tagName eq "id")
        {
        	$id = $tagValue;
        }
        elsif ($tagName eq "url")
        {
        	$url = resolvePropertyValue($tagName, $tagValue, $propsRef);
        }
    }
}

# ---------------------------------------------------------------------------- 

# Return: (lineNumber, pluginId, version) - plugin/version may be undef if no information extracted
sub readPluginData {
    my($filePath,$fileHandle,$lineNumber,$propsRef,$pluginsRef)=@_;
    my $line=undef;
    my $tagName=undef;
    my $tagValue=undef;
    my $endElement=0;
	my $groupId=undef;
	my $artifactId=undef;
	my $version=undef;

    while(1)
    {
        ($lineNumber,$line) = readNextLine($fileHandle, $lineNumber);
        if (!defined($line))
        {
            dieWithMessage(85, "Premature EOF while read properties");
        }

        # Check if comment start
        ($lineNumber,$line) = adjustIfCommentOrCDATA($fileHandle, $lineNumber, $line);
        if (!defined($line))
        {
            next;
        }

        if ($line =~ /^<\/plugin>/)
        {
        	if (!defined($artifactId))
        	{
        		dieWithMessage(50, "No plugin artifact id");
        	}
        	
        	if (!defined($version))
        	{
        		return ($lineNumber, undef, undef);
        	}

            if (!defined($groupId))
            {
             	$groupId = "org.apache.maven.plugins";
            }

      	    my  $pluginId = $groupId . ":" . $artifactId;
       	    $pluginsRef->{ $pluginId } = $version;
        	return ($lineNumber, $pluginId, $version);
        }
        
        # skip rest of plugin configuration if have what we need
        if (defined($groupId) && (defined($artifactId)) && (defined($version)))
        {
        	next;
        }
        
        ($lineNumber,$tagName,$tagValue,$endElement) = parseElementLine($filePath, $fileHandle, $lineNumber, $line);
        # TODO check if already set
        # TODO ensure tag value defined and not empty
        if ($tagName eq $MVN_GROUPID)
        {
        	$groupId = $tagValue;
        }
        elsif ($tagName eq $MVN_ARTIFACTID)
        {
            $artifactId = $tagValue;        	
        }
        elsif ($tagName eq $MVN_VERSION)
        {
        	$version = resolvePropertyValue($tagName, $tagValue, $propsRef);
        }
    }
}

# ---------------------------------------------------------------------------- 

# Return: line number where properties ended
sub populateProperties {
    my($filePath,$fileHandle,$usedOptionsRef,$lineNumber,$propsRef)=@_;
    my $ignoreDuplicateProps=exists($usedOptionsRef->{ $OPT_IGNORE_DUP_PROPS });
    my $verbose=exists($usedOptionsRef->{ $OPT_VERBOSE });
    my $line=undef;
    my $tagName=undef;
    my $tagValue=undef;
    my $endElement=0;

    while(1)
    {
        ($lineNumber,$line) = readNextLine($fileHandle, $lineNumber);
        if (!defined($line))
        {
        	dieWithMessage(85, "Premature EOF while read properties");
        }

        # Check if comment start
        ($lineNumber,$line) = adjustIfCommentOrCDATA($fileHandle, $lineNumber, $line);
        if (!defined($line))
        {
            next;
        }
        
        ($lineNumber,$tagName,$tagValue,$endElement)=parseElementLine($filePath, $fileHandle, $lineNumber, $line);
       	if ($tagName eq $MVN_PROPERTIES)
       	{
            if ($endElement == 0)
            {
        		dieWithMessage(89, "Unexpected sub-properties at line $lineNumber: $line");
            }
            
            resolvePropertiesValues($propsRef);
            return $lineNumber;
       	}
       	
       	if ($endElement == 0)
       	{
            dieWithMessage(88, "Multi-line property value at line $lineNumber: $line");
       	}
       	
       	if (exists($propsRef->{ $tagName }))
       	{
       		if ($ignoreDuplicateProps)
       		{
       			if ($verbose)
       			{
       				print "Property $tagName value re-defined at line $lineNumber: $line\n";
       			}
       		}
       		else
       		{
                dieWithMessage(87, "Property $tagName value re-defined at line $lineNumber: $line");
       		}
       		
       		next;
       	}
       	
       	if (!defined($tagValue))
       	{
            dieWithMessage(87, "Property $tagName has no value at line $lineNumber: $line");
       	}

       	$propsRef->{ $tagName } = $tagValue;
    }

    dieWithMessage(90, "No end of properties section detected");
}

sub resolvePropertiesValues {
	my($propsRef)=@_;
    my %resolvedProps=();

    # LOG print "Properties:\n";
    while(my($key,$value)=each $propsRef)
    {
        my $altValue=resolvePropertyValue($key, $value, $propsRef);
        if ($altValue ne $value)
        {
            $resolvedProps{ $key } = $altValue;
            # LOG print "\t$key: $value => $altValue\n";
        }
        else
        {
            # LOG print "\t$key: $value\n";
        }
    }

    while(my($key,$value)=each %resolvedProps)
    {
        $propsRef->{ $key } = $value;
    }
    
    return $propsRef;
}

# ---------------------------------------------------------------------------- 

# Parameters: file handle, current line number, current line data (trimmed)
# Return: line number, line data if not a comment - undef otherwise
sub adjustIfCommentOrCDATA {
	my($fileHandle,$lineNumber,$line)=@_;

    if ($line =~ /^<!--/)
    {
        # Check if one line comment
        if ($line !~ /-->$/)
        {
           ($lineNumber, $line) = skipTillEndOfComment($fileHandle,$lineNumber);
        }

        return ($lineNumber, undef);
    }
    elsif ($line =~ /^<!\[CDATA\[/)
    {
    	# check if one line CDATA
    	if ($line !~ /\]\]>$/)
    	{
           ($lineNumber, $line) = skipTillEndOfCDATA($fileHandle,$lineNumber);
    	}

        return ($lineNumber, undef);
    }

    return ($lineNumber, $line);	
}

# ---------------------------------------------------------------------------- 

# Parameters: line containing XML element (trimmed and non-empty)
# Return: (lineNumber,tagName,tagValue,endElement) - endElement=1 if ending element tag
#       Note: if only tag name exists then tag value is undef
sub parseElementLine {
	my($filePath,$fileHandle,$lineNumber,$line)=@_;

    if (substr($line, 0, 1) ne '<')
    {
    	dieWithMessage(98, "[$filePath]: Invalid element value (no start delimiter) at line $lineNumber: $line");
    }
    
    my $startPos=1, $endElement=0;
    if (substr($line, 1, 1) eq '/')
    {
    	$startPos++;
    	$endElement = 1;
    }
    
    my $endPos=index($line, '>', $startPos);
    if ($endPos <= $startPos)
    {
        dieWithMessage(99, "[$filePath]: Invalid element value (start tag - no end delimiter) at line $lineNumber: $line");
    }
    
    my $tagName=trim(substr($line, $startPos, $endPos - $startPos));
    if (length($tagName) <= 0)
    {
        dieWithMessage(100, "[$filePath]: Invalid element value (no tag name) at line $lineNumber: $line");
    }

    # check if pure ending element    
    if ($endElement != 0)
    {
    	return ($lineNumber, $tagName, undef, $endElement);
    }

    my $tagValue=undef;
    # check if element value on same line as tag
    if ($endPos < (length($line) - 1))
    {
        my $remainder=substr($line, $endPos + 1);

        $startPos = index($remainder, '<');
        if ($startPos < 0)
        {
            dieWithMessage(95, "[$filePath]: Invalid element value (no tag end) at line $lineNumber: $line");
        }
        
        $startPos++;
        
        my $contChar=substr($remainder, $startPos, 1);
        if ($contChar eq '!')   # check if continuation is a remark
        {
        	$endPos = index($remainder, '>', $startPos + 1);
        	if ($endPos < 0)
        	{
        	   ($lineNumber, $remainder) = skipTillEndOfComment($fileHandle, $lineNumber);	
        	}
        	
        	return ($lineNumber, $tagName, undef, $endElement);
        }

        if ($contChar ne '/')
        {
            dieWithMessage(93, "Invalid element value (end tag - no end signal) at line $lineNumber: $line");
        }

        $tagValue = trim(substr($remainder, 0, $startPos - 1));

	    my $endPos=index($remainder, '>', $startPos + 1);
	    if ($endPos <= $startPos)
	    {
            dieWithMessage(94, "[$filePath]: Invalid element value (end tag - no end delimiter) at line $lineNumber: $line");
	    }
	    
	    my $endTag=trim(substr($remainder, $startPos + 1, $endPos - $startPos - 1));
	    if ($tagName ne $endTag)
	    {
            dieWithMessage(92, "[$filePath]: Invalid element value (end tag - mismatched name) at line $lineNumber: $line");
	    }
	    
	    $endElement = 1;
    }
    
    return ($lineNumber, $tagName, $tagValue, $endElement);
}

# ---------------------------------------------------------------------------- 

sub skipProjectElement {
    my($fileHandle,$lineNumber)=@_;
    my $line=undef;
    
    while(1)
    {
        ($lineNumber,$line) = readNextLine($fileHandle, $lineNumber);
        if (!defined($line))
        {
        	dieWithMessage(102, "Premature EOF while looking for project element data end");
        }

        if (index($line, '>') >= 0)
        {
            return ($lineNumber, $line);
        }
    }

    dieWithMessage(101, "No end of project start element found"); 
}

# ---------------------------------------------------------------------------- 

# Parameters: open file handle
# Return: (lineNumber,line) where end of multi-line XML comment found
sub skipTillEndOfComment {
	my($fileHandle,$lineNumber)=@_;
    my $line=undef;
    
    while(1)
    {
    	($lineNumber,$line) = readNextLine($fileHandle, $lineNumber);
    	if (!defined($line))
    	{
            dieWithMessage(100, "No end of comment found"); 
    	}

        if ($line =~ /-->$/)
        {
            return ($lineNumber, $line);
        }
    }
}

sub skipTillEndOfCDATA {
    my($fileHandle,$lineNumber)=@_;
    my $line=undef;
    
    while(1)
    {
        ($lineNumber,$line) = readNextLine($fileHandle, $lineNumber);
        if (!defined($line))
        {
            dieWithMessage(100, "No end of comment found"); 
        }

        if ($line =~ /\]\]>$/)
        {
            return ($lineNumber, $line);
        }
    }
}

# ---------------------------------------------------------------------------- 

# Parameters: file handle, current line number
# Return: line number, next non-empty line (trimmed) - undef if EOF
sub readNextLine {
	my($fileHandle,$lineNumber)=@_;

    while(defined(my $line=<$fileHandle>))
    {
        $lineNumber++;
        chomp($line);
        $line = trim($line);
        
        if (length($line) > 0)
        {
        	return ($lineNumber,$line);
        }
    }
    
    return ($lineNumber, undef);    # signal EOF
}

# ---------------------------------------------------------------------------- 

# Parameters: property name, value, properties reference
# Return: resolved value where ${xxx} is replaced by the actual value
sub resolvePropertyValue {
    my($propName,$propValue,$propsRef)=@_;

    while(1)
    {
	    my $replStart=index($propValue, '${', 0);
	    if($replStart < 0)
	    {
	    	return $propValue;
	    }
	
	   	my $replEnd=index($propValue, '}', $replStart + 2);
	   	if ($replEnd < 0)
	   	{
	   		return $propValue;
	   	}
	    	
	   	my $altProp=substr($propValue, $replStart + 2, $replEnd - $replStart - 2);
	   	if (!exists($propsRef->{ $altProp }))
	   	{
	   		return $propValue;
	   	}
	
	    my  $altVal=$propsRef->{ $altProp };
		my  $propPrefix="", $propSuffix="";
	    if ($replStart > 0)
	    {
	       $propPrefix = substr($propValue, 0, $replStart);
	    }
	    	   
	    if ($replEnd < (length($propValue) - 1))
	    {
	       $propSuffix=substr($propValue, $replEnd + 1);    	   	
	    }
	  
	    $propValue = $propPrefix . $altVal . $propSuffix;
    }
}

# ---------------------------------------------------------------------------- 

$MVN_METDATA_LATEST="latest";
$MVN_METDATA_RELEASE="release";

# Return: (version,reason,repo-id,repo-url) - if better version found then reason/repo-id/repo-url are defined
sub checkPluginVersionAvailability {
	my($usedOptionsRef, $groupId, $artifactId, $version, $reposRef)=@_;
	my $verbose=exists($usedOptionsRef->{ $OPT_VERBOSE });
    my $curVersion=$version;
    my @curComps=split(/\./, $curVersion);
    my $reason=undef;
    my $curId=undef;
    my $curUrl=undef;
    my $groupPath = $groupId;
    $groupPath =~ s/\./\//g;

	while(my($id,$url)=each $reposRef)
	{
		if ($verbose)
		{
	       print "Checking $groupId:$artifactId:$curVersion at $url\n";
		}
		
		my $pluginFolder=$url;
		if (substr($pluginFolder, length($pluginFolder) - 1, 1) eq '/')
		{
			$pluginFolder = $pluginFolder . $groupPath;
		}
		else
	    {
            $pluginFolder = $pluginFolder . "/" . $groupPath;
	    }
	    
	    $pluginFolder = $pluginFolder . "/" . $artifactId . "/maven-metadata.xml";
	    
	    if ($verbose)
	    {
	       print "\tChecking $id [$pluginFolder]\n";
	    }

        my @availableVersions=();
        my %preferredVersions=();
        readMavenMetadata($pluginFolder, \@availableVersions, \%preferredVersions);

        # If have a released version then use it (if better than existing)
        if (exists($preferredVersions{ $MVN_METDATA_RELEASE }))
        {
        	my $altVersion=$preferredVersions{ $MVN_METDATA_RELEASE };
            my @altComps=split(/\./, "$altVersion");
            my $cmpResult=compareVersions($curVersion, \@curComps, $altVersion, \@altComps);
            if ($cmpResult < 0)
            {
                $reason = $MVN_METDATA_RELEASE;
                if ($verbose)
                {
                    print "\t\t$groupId:$artifactId [$reason]: $curVersion => $altVersion\n";
                }
                $curVersion = $altVersion;
                @curComps = @altComps;
                $curId = $id;
                $curUrl = $url;
            }

            last;            
        }
        
        foreach my $altVersion (@availableVersions)
        {
        	if ($verbose)
        	{
                print "\t\t$groupId:$artifactId [CANDIDATE]: $curVersion => $altVersion\n";
        	}

        	my @altComps=split(/\./, $altVersion);
            my $cmpResult=compareVersions($curVersion, \@curComps, $altVersion, \@altComps);
        	if ($cmpResult < 0)
        	{
                $reason = $MVN_METDATA_LATEST;
                if ($verbose)
                {
                    print "\t\t\t$groupId:$artifactId [$reason]: $curVersion => $altVersion\n";
                }
        		$curVersion = $altVersion;
        		@curComps = @altComps;
                $curId = $id;
                $curUrl = $url;
        	}
        }
	}

	return ($curVersion, $reason, $curId, $curUrl); 
}

# ---------------------------------------------------------------------------- 

sub parseMavenMetadataVersions {
    my($filePath,$fileHandle,$versionsRef,$preferredRef)=@_;
    my $lineNumber=0;
    my $line=undef;
    my $tagName=undef;
    my $tagValue=undef;
    my $endElement=0;
    my $latest=undef;
    my $release=undef;
    my $collectEnabled=0;

    while(1)
    {
        ($lineNumber,$line) = readNextLine($fileHandle, $lineNumber);
        if (!defined($line))
        {
            return $lineNumber;
        }

        # Check if comment start
        ($lineNumber,$line) = adjustIfCommentOrCDATA($fileHandle, $lineNumber, $line);
        if (!defined($line))
        {
            next;
        }
        
        ($lineNumber,$tagName,$tagValue,$endElement)=parseElementLine($filePath, $fileHandle, $lineNumber, $line);
        if ($tagName eq "versioning")
        {
            if ($endElement != 0)
            {
                # TODO return also the release and latest
                return $lineNumber;  # not interested in anything else beyond that
            }
            
            $collectEnabled = 1;
            next;
        }

        if ($collectEnabled <= 0)
        {
            next;  # wait till we see the versioning element
        }

        # TODO ensure that tag value is defined / non-empty
        if ($tagName eq $MVN_VERSION)
        {
            push($versionsRef, trim($tagValue)); 
        }
        elsif (($tagName eq $MVN_METDATA_LATEST)
            || ($tagName eq $MVN_METDATA_RELEASE))
        {
            $preferredRef->{ $tagName } = trim($tagValue);
            $latest = $tagValue;
        }
    }
}

sub readMavenMetadata {
    my($pluginFolder,$versionsRef,$preferredRef)=@_;
    my $lineNumber=0;
    my $line=undef;

    # redirect STDERR as well so that we can detect the "not a working copy" message(s)
    open my $PROG, "wget -q --no-check-certificate -O - $pluginFolder 2>&1 |" || dieWithMessage(40, "Could not execute WGET command on $pluginFolder: ".$!);
    while(1)
    {
        ($lineNumber,$line) = readNextLine($PROG, $lineNumber);
        if (!defined($line))
        {
            # TODO print a warning
            last;
        }
        
        # LOG print ">>> $lineNumber: $line\n";
        if ($line =~ /^<\?xml/)  # wait till XML starts
        {
            parseMavenMetadataVersions($pluginFolder, $PROG, $versionsRef, $preferredRef);
        }
    }

    close($PROG);
}

# ---------------------------------------------------------------------------- 

sub collectPluginsRecommendations {
	my($usedOptionsRef,$reposRef,$pluginsRef)=@_;

    foreach my $pluginId (keys $pluginsRef)
    {
        my $sepPos=index($pluginId, ':');
        my $groupId=substr($pluginId, 0, $sepPos);
        my $artifactId=substr($pluginId, $sepPos + 1);
        my $version=$pluginsRef->{ $pluginId };
        my($curVersion, $reason, $curId, $curUrl)=checkPluginVersionAvailability($usedOptionsRef, $groupId, $artifactId, $version, $reposRef);

        if (defined($reason))
        {
        	print "\t$groupId:$artifactId:$version => $curVersion  from $curId [$curUrl]\n";
        }   	
    }
}

# ---------------------------------------------------------------------------- 

sub compareVersions {
	my($v1,$v1Ref,$v2,$v2Ref)=@_;
	if ($v1 eq $v2)
	{
		return 0;
	}

	my $l1=$#$v1Ref;
	my $l2=$#$v2Ref;
	my $cmpLen=($l1 < $l2) ? $l1 : $l2;
	
	for (my $index=0; $index <= $cmpLen; $index++)
	{
		my $a1=$$v1Ref[$index], $a2=$$v2Ref[$index];
		if ($a1 eq $a2)
		{
			next;
		}
		
		my $n1=int($a1), $n2=int($a2);
		return ($n1 - $n2);
	}
	
	return ($l1 - $l2);    # shorter one is smaller
}

# ---------------------------------------------------------------------------- 

sub testVersionsComparison {
    my $TEST_VALUE="2.1.1";
    my @zzz=("2.1.2", "1.1", "1.2.0", $TEST_VALUE, "2.1", "2.2", "3", "3.1" );
    my @aaa=split(/\./, $TEST_VALUE);
    foreach my $z (@zzz)
    {
		my @zv=split(/\./, $z);
		my $result=compareVersions($TEST_VALUE, \@aaa, $z, \@zv);
		print "Compare with $z: $result\n";
    }
    exit;
}

sub testParseMavenMetadata {
	my($filePath)=@_;
	my @availableVersions=(), %preferredVersions=();
	open(my $FILE, $filePath) || die "Failed to open test file $filePath: $!";
	parseMavenMetadataVersions($filePath, $FILE, \@availableVersions, \%preferredVersions);
	close($FILE);
	
	foreach my $version (@availableVersions)
	{
		print "\t$version\n";
	}
	while(my($key,$value)=each %preferredVersions)
	{
		print "\t$key : $value\n";
	}
	exit;
}

############################# MAIN ########################################### 

my %usedOptions=(), %propertiesValues=(), %orgRepos=();
parseCommandLineOptions(\%usedOptions, \%propertiesValues, \%orgRepos, \@ARGV);

my %pluginRepos=();
if (!exists($usedOptions{ $OPT_URL_IGNORE }))
{
	%pluginRepos = %orgRepos;
}

my $pomFilePath=$usedOptions{ $OPT_POM_FILE }, %plugins=();
print "Processing $pomFilePath\n";
extractPomInformationNodes($pomFilePath, \%usedOptions, \%propertiesValues, \%pluginRepos, \%plugins);
if (exists($usedOptions{ $OPT_URL_IGNORE }))
{
    %pluginRepos = %orgRepos;
}

collectPluginsRecommendations(\%usedOptions, \%pluginRepos, \%plugins);
exit 0; # just to have a breakpoint before exit
