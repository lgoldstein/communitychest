/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.auth.dh;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * <P>Executes the Diffie-Hellman required calculations</P>
 * 
 * @author Lyor G. - based on code in com.jcraft.jsch.jce.DH
 * @since Jun 16, 2010 2:11:52 PM
 */
public class DhExchangeCalculator {
	private BigInteger	_generator;
	public BigInteger getGenerator ()
	{
		return _generator;
	}

	public void setGenerator (BigInteger generator)
	{
		_generator = generator;
	}
	
	private BigInteger	_safePrime;
	public BigInteger getSafePrime ()
	{
		return _safePrime;
	}

	public void setSafePrime (BigInteger safePrime)
	{
		_safePrime = safePrime;
	}

	private BigInteger	_eValue;
	public BigInteger getEValue ()
	{
		return _eValue;
	}

	public void setEValue (BigInteger eValue)
	{
		_eValue = eValue;
	}
	// NOTE: does not set the E-value member
	public BigInteger calculateEValue ()
		throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException
	{
	    final DHParameterSpec	dhSkip=
	    	new DHParameterSpec(getSafePrime(), getGenerator());
		final KeyPairGenerator	kpGen=KeyPairGenerator.getInstance("DH");
		kpGen.initialize(dhSkip);

		final KeyPair		kp=kpGen.generateKeyPair();
		final KeyAgreement	keyAgree=KeyAgreement.getInstance("DH");
		final PrivateKey	privateKey=kp.getPrivate();
		keyAgree.init(privateKey);

		final PublicKey	publicKey=kp.getPublic();
	    final byte[]	pubKeyEnc=publicKey.getEncoded();
	    if ((null == pubKeyEnc) || (pubKeyEnc.length <= 0))
	    	throw new InvalidKeyException("No public key encoded bytes");
	    if (!(publicKey instanceof DHPublicKey))
	    	throw new InvalidAlgorithmParameterException("Public key not " + DHPublicKey.class.getSimpleName() + " (" + publicKey.getClass().getName() + ")");

	    return ((DHPublicKey) publicKey).getY();
	}

	private BigInteger	_fValue;
	public BigInteger getFValue ()
	{
		return _fValue;
	}

	public void setFValue (BigInteger fValue)
	{
		_fValue = fValue;
	}


	private BigInteger	_sharedKey;
	public BigInteger getSharedKey ()
	{
		return _sharedKey;
	}

	public void setSharedKey (BigInteger sharedKey)
	{
		_sharedKey = sharedKey;
	}
	// NOTE: does not set the shared key value
	public BigInteger calculateSharedKey ()
		throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException
	{
		final KeyFactory 		keyFac=KeyFactory.getInstance("DH");
	    final DHPublicKeySpec	keySpec=
	    	new DHPublicKeySpec(getFValue(), getSafePrime(), getGenerator());
	    final PublicKey			pubKey=keyFac.generatePublic(keySpec);
		final KeyAgreement		keyAgree=KeyAgreement.getInstance("DH");
		keyAgree.doPhase(pubKey, true);

		final byte[] sharedSecret=keyAgree.generateSecret();
	    return new BigInteger(sharedSecret);
	}

}
