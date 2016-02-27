#ifndef _UTL_MATH_H
#define _UTL_MATH_H
/*
 * File
 *		math.h
 *
 * Purpose
 *
 *			Contains declarations of useful mathematical functions
 *
 * Programmer
 *		LYOR	G.
 *
 * Current Revision
 *		$Revision: 561 $
 *
 * Date
 *		10-Sep-1995
 */

/*
 * History of changes:
 * 07-Nov-1995 ( Alon G. ) added copy and equel operator to RATIO.
 */

/*---------------------------------------------------------------------------*/

#include <util/string.h>

/*---------------------------------------------------------------------------*/

/*
 * Compute GCD using Euclides algorithm
 */

extern DWORD gcd (const DWORD n1, const DWORD n2);

/*
 *	Compute LCM using the equivalence : a * b = GCD(a,b) * LCM(a,b)
 */

extern DWORD lcm (const DWORD n1, const DWORD n2);

/*---------------------------------------------------------------------------*/

extern BOOLEAN IsPrimeNumber (const UINT32 v);
extern UINT32 FindClosestPrime (const UINT32 v);
extern UINT32 GetIntegerSquareRoot (const UINT32 v);

/*
 * Separator used in strings encoding a ratio of the form "a/b"
 */

#define RATIO_COMP_SEP '/'

#ifdef __cplusplus
// this object implements the idea of RATIO numbers - i.e. numbers having
// the form of a/b, where (a,b) are POSITIVE integers
class RATIO {
	private:
		UINT32 en; // enumerator
		UINT32 dn; // denominator

	public:
	// constructor - if no arguments supplied, then initializes the ratio to 1/1
	//		this is done in order to avoid a denominator of '0' which may cause
	//		problems when dividing (i.e. zero divide error). This initialization
	//		also causes the defined instance to be "neutral" - i.e. if it further
	//		participates in ratio computation it has no effect (at least as far as
	//		division and multiplication are concerned - which is our main concern
	//		so as to avoid a zero divide error).
		RATIO (UINT32 e=1, UINT32 d=1)
		{
			en = e;
			dn = d;
			minimize();
		}

		RATIO (const RATIO& r)
			: en(r.en), dn(r.dn)
		{
			minimize();
		}

		RATIO& operator= (const RATIO& r)
		{
			en = r.en;
			dn = r.dn;

			return minimize();
		}

	// this member function finds the smallest possible representation for the
	// ratio - i.e. it seeks to reduce the denominator as much as possible.
	// Although it is defined as a public member it has no real effect because
	// the resulting ratio is always reduced to its minimal representation.
		RATIO& minimize ()
		{
			DWORD g=gcd(en,dn);
	
			//	  The best way to minimize a ratio representation is to check the
			// GCD of components. If they are not at their minimal value, then
			// GCD > 1, and we can use the GCD to minimize them.

			if (g > 1)
			{
				en /= g;
				dn /= g;
			}
	
			return(*this);
		}

		const RATIO& values (DWORD& e, DWORD& d) const
		{
			e = en;
			d = dn;

			return (*this);
		}

	// useful operators
		RATIO& operator *= (const RATIO& r)
		{
			en *= r.en;
			dn *= r.dn;
			return(minimize());
		}

		RATIO& operator *= (UINT32 n)
		{
			en *= n;
			
			return(minimize());
		}

		RATIO& operator /= (const RATIO& r)
		{
			// dividing 2 rational numbers is the same as multiplying them
			// cross-wise with each other

			en *= r.dn;
			dn *= r.en;

			return(minimize());
		}

		RATIO& operator /= (UINT32 n)
		{
			// dividing a ratio by a constant is the same as multiplying its
			// denominator by this constant

			dn *= n;
			return(minimize());
		}

		RATIO& operator += (const RATIO& r)
		{
			DWORD l=lcm(dn, r.dn);		  // find least common multiple
			DWORD f1=l/dn, f2=r.dn/dn;	  // find factors for enumerators

			en = en * f1 + r.en * f2;
			dn = l;
	
			return(minimize());
		}

	// NOTE: if result becomes negative as a result, then nothing is done
		RATIO& operator -= (const RATIO& r)
		{
			DWORD l=lcm(dn, r.dn);		  // find least common multiple
			DWORD f1=l/dn, f2=r.dn/dn;	  // find factors for enumerators
			DWORD e1=en * f1, e2=r.en * f2;

			if (e1 > e2)
				en = e1 - e2;
			else
				return(*this); // if result becomes negative do not perform it
			dn = l;

			return(minimize());
		}

	// friends

	// comparison operators - the operators work under the assumption that both
	// ratios have been minimized (which is implicit due to the nature of the
	// implementation).
		friend BOOLEAN operator == (const RATIO& r1, const RATIO& r2)
		{
			if ((r1.en == r2.en) && (r1.dn == r2.dn))
				return(TRUE);
			else
				return(FALSE);
		}

		friend BOOLEAN operator != (const RATIO& r1, const RATIO& r2)
		{
			if (r1 == r2)
				return(FALSE);
			else
				return(TRUE);
		}

		friend BOOLEAN operator < (const RATIO& r1, const RATIO& r2)
		{
			// the bigger the denominator the smaller the ratio

			if (r1.dn > r2.dn)
				return(TRUE);

			if (r1.dn < r2.dn)
				return(FALSE);
			
			// at this point the denominators are the same - check enumerators
			if (r1.en < r2.en)
				return(TRUE);
			else
				return(FALSE);
		}

		friend BOOLEAN operator > (const RATIO& r1, const RATIO& r2)
		{
			// the bigger the denominator the smaller the ratio

			if (r1.dn < r2.dn)
				return(TRUE);

			if (r1.dn > r2.dn)
				return(FALSE);
			
			// at this point the denominators are the same - check enumerators
			if (r1.en > r2.en)
				return(TRUE);
			else
				return(FALSE);
		}

		friend BOOLEAN operator <= (const RATIO& r1, const RATIO& r2)
		{
			// the bigger the denominator the smaller the ratio

			if (r1.dn > r2.dn)
				return(TRUE);

			if (r1.dn < r2.dn)
				return(FALSE);
			
			// at this point the denominators are the same - check enumerators
			if (r1.en <= r2.en)
				return(TRUE);
			else
				return(FALSE);
		}

		friend BOOLEAN operator >= (const RATIO& r1, const RATIO& r2)
		{
			// the bigger the denominator the smaller the ratio

			if (r1.dn < r2.dn)
				return(TRUE);

			if (r1.dn > r2.dn)
				return(FALSE);
			
			// at this point the denominators are the same - check enumerators
			if (r1.en >= r2.en)
				return(TRUE);
			else
				return(FALSE);
		}

	// arithmetical operations
		friend RATIO operator + (const RATIO& r1, const RATIO& r2)
		{
			RATIO res(r1);
			
			res += r2;
			return(res);
		}
		
		friend RATIO operator - (const RATIO& r1, const RATIO& r2)
		{
			RATIO res(r1);
			
			res -= r2;
			return(res);
		}
		
		friend RATIO operator * (const RATIO& r1, const RATIO& r2)
		{
			RATIO res(r1);
			
			res.en *= r2.en;
			res.dn *= r2.dn;
			return(res.minimize());
		}

	// multiplying with a constant number only affects the enumerator
		friend RATIO operator * (const RATIO& r, UINT32 n)
		{
			RATIO res(r);

			res *= n;
			return(res);
		}
		
		friend RATIO operator / (const RATIO& r1, const RATIO& r2)
		{
			RATIO res(r1);
			
			res.en *= r2.dn;
			res.dn *= r2.en;
			
			return(res.minimize());
		}

	// dividing by a constant number only affects the denominator
		friend RATIO operator / (const RATIO& r, UINT32 n)
		{
			RATIO res(r);

			res.dn *= n;
			return(res.minimize());
		}

		// conversion functions between strings and ratios
		friend size_t ratio2str (const RATIO& r, char str[]);
		friend EXC_TYPE str2ratio (const char str[], RATIO& r);
}; // end of RATIO class definition
#endif /* of ifdef C++ */

/*
 * Maximum number of chars in a ratio string - not including '\0'
 */

#define RATIO_STR_MAX_LEN ((2*MAX_DWORD_DISPLAY_LENGTH)+1)

/*---------------------------------------------------------------------------*/

#endif /* of ifdef MATH_H */
