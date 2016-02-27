/*
 * 
 */
package net.community.chest.groovy

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 25, 2010 7:54:43 AM
 */
class GroovyApplicationUtils {
	/**
	 * Parses a list of string values received at the command line. It assumes
	 * that the list contains some options followed by 'arguments' - e.g.,
	 * <code>--user foo -pw=bar -v -x=file1.xml file2.doc</code>. The options may
	 * be either unary or binary. A binary option accepts either the next argument
	 * as or an argument separayed by '='. <B>Note:</B> a unary option cannot be
	 * last one before the arguments since it will be interpreted as a binary
	 * @param prefixes A list of allowed prefixes - e.g., [ '-', '--', '/ ]
	 * @param args The values list
	 * @return A list (or null) of 2 elements: position 0=options map, 1=remainder
	 * of values. The options map has key=pure option name (i.e., without the prefix)
	 * and value=list of values for the option. <B>Note:</B> the mapped value for a
	 * unary option is the option itself (i.e., ["v":[v]]).
	 */
	static parseCommandLine (prefixes, args) {
		def numArgs=(args == null) ? 0 : args.size
		if (numArgs <= 0) {
			return null
		}
		
		if ((prefixes == null) || (prefixes.size <= 0)) {
			return [ null, args ]
		}
		
		def optsMap=[:]
		for (def aIndex=0; aIndex < numArgs; aIndex++) {
			def argVal=args[aIndex]
			def argPrefix=getLongestPrefixMatch(argVal, true, prefixes)
			// if no prefix assume start of arguments
			if ((null == argPrefix) || (argPrefix.length() <= 0)) {
				def subRange=new IntRange(aIndex, numArgs - 1)
				return [ optsMap, args.getAt(subRange) ]
			}
			
			argVal = argVal.substring(argPrefix.length())	// strip the prefix
			def sepPos=argVal.indexOf('=')
			def optValue=null
			// check if binary option value specified inline as a=b
			if ((sepPos > 0) && (sepPos < (argVal.length() - 1))) {
				optValue = argVal.substring(sepPos + 1)
				argVal = argVal.substring(0, sepPos)
			}
			
			def optVals=optsMap[argVal]
			if (null == optVals) {
				optVals = []
				optsMap[argVal] = optVals
			}

			// if unary value			
			if (optValue == null) {
				optValue = argVal	// use some default even if unary
				
				if (aIndex < numArgs) {
					optValue = args[aIndex + 1]

					def nextPrefix=getLongestPrefixMatch(optValue, true, prefixes)
					if ((nextPrefix == null) || (nextPrefix.length() <= 0)) {
						aIndex++
					} else {	// unary - restore the default
						optValue = argVal
					}
				}
			}

			optVals.add(optValue)
		}

		// this point is reached if only options specified with no arguments		
		return [ optsMap, null ]
	}
	/**
	 * @param argVal String to be checked
	 * @param strictPrefix If <code>true</code> then the prefix must be strict - i.e.,
	 * some characters left after peeling off the prefix
	 * @param prefixes Collection of prefixes to check
	 * @return Longest possible matching prefix - null/empty if no match
	 */
	static getLongestPrefixMatch (argVal, strictPrefix, prefixes) {
		if ((argVal == null) || (argVal.length() <= 0)
		|| (prefixes == null) || (prefixes.size <= 0)) {
			return null
		}
		
		def argPrefix=null
		for (p in prefixes) {
			if ((p == null) || (p.length() <= 0)) {
				continue;
			}

			if (!argVal.startsWith(p)) {
				continue;
			}

						// skip if not strict prefix
			if  (strictPrefix && (argVal.length() <= p.length())) {
				continue;
			}

			// skip to next if current prefix longer than candidate
			if ((argPrefix != null) && (argPrefix.length() > p.length())) {
				continue;
			}
			
			argPrefix = p
		}

		return argPrefix
	}
}
