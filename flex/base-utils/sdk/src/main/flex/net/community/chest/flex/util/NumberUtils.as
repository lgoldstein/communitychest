package net.community.chest.flex.util
{
	public final class NumberUtils
	{
		public static function isUnsignedIntegerString (s:String):Boolean
		{
			if ((null == s) || (s.length <= 0))
				return false;

			for (var idx:int=0; idx < s.length; idx++)
			{
				var	c:String=s.charAt(idx);
				if ((null == c) || (c.length != 1))
					return false;	// debug breakpoint
				if ((c < "0") || (c > "9"))
					return false;
			}

			return true;
		}

		public static function isSignedIntegerString (s:String):Boolean
		{
			if ((null == s) || (s.length <= 0))
				return false;

			var sgn:String=s.charAt(0);
			if ((null == sgn) || (sgn.length <= 0))
				return false;	// debug breakpoint

			if (("+" == sgn) || ("-" == sgn))
			{
				// not allowed to have only the sign
				if (s.length <= 1)
					return false;

				return isUnsignedIntegerString(s.substring(1, s.length));
			}

			return isUnsignedIntegerString(s);
		}
	}
}