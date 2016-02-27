package com.cti2.toolkits.internal.util.internet;

import com.cti2.toolkits.internal.util.general.*;

// holds rfc822 related strings
public class RFC822Strs {

	/*---------------- headers related strings -------------*/

	public static final String stdFromHdr="From:";
	public static final String stdSenderHdr="Sender:";
	public static final String stdToHdr="To:";
	public static final String stdCcHdr="Cc:";
	public static final String stdBccHdr="Bcc:";
	public static final String stdDateHdr="Date:";
	public static final String stdMIMEVersionHdr="MIME-Version:";
	public static final String stdReturnPathHdr="Return-Path:";
	public static final String stdReplyToHdr="Reply-To:";
	public static final String stdInReplyToHdr="In-Reply-To:";
	public static final String stdReceivedHdr="Received:";
	public static final String stdSubjectHdr="Subject:";
	public static final String stdContentTypeHdr="Content-Type:";
	public static final String stdContentLengthHdr="Content-Length:";
	public static final String stdContentLocationHdr="Content-Location:";
	public static final String stdContentBaseHdr="Content-Base:";
	public static final String stdContentIDHdr="Content-ID:";
	public static final String stdContentXferEncoding="Content-Transfer-Encoding:";
	public static final String stdContentDisposition="Content-Disposition:";
	public static final String stdContentDescription="Content-Description:";
	public static final String stdContentDuration="Content-Duration:";
	public static final String stdContentLanguage="Content-Language:";
	public static final String stdContentMD5Hdr="Content-MD5:";
	public static final String stdMessageIDHdr="Message-ID:";
	public static final String stdImportanceHdr="Importance:";
	public static final String stdSensitivityHdr="Sensitivity:";
	public static final String stdStatusHdr="Status:";
	public static final String stdReturnReceiptToHdr="Return-Receipt-To:";
	public static final String stdReturnReceiptCcHdr="Return-Receipt-Cc:";
	public static final String stdDispositionNotificationToHdr="Disposition-Notification-To:";

	public static final String stdResentFromHdr="Resent-From:";
	public static final String stdResentSenderHdr="Resent-Sender:";
	public static final String stdResentToHdr="Resent-To:";
	public static final String stdResentCcHdr="Resent-Cc:";
	public static final String stdResentDateHdr="Resent-Date:";
	public static final String stdResentMessageIDHdr="Resent-Message-ID:";

	public static final String stdApparentlyToHdr="Apparently-To:";
	public static final String stdApparentlyCcHdr="Apparently-Cc:";
	public static final String stdApparentlyBccHdr="Apparently-Bcc:";

		/* some "well-known" X-hdrs */
	public static final String XPriorityHdr="X-Priority:";
	public static final String XMailerHdr="X-Mailer:";

	/*---------------- MIME related strings -------------*/

	public static final String MIMEBoundaryDelims="--";

   public static final char RFC822_MIMETAG_SEP='/';
   public static final char RFC822_KEYWORD_VALUE_DELIM='=';

		/* some well known type values */

		/* some standard sub-types */
	public static final String MIMEApplicationType="application";
	public static final String MIMEMultipartType="multipart";
	public static final String MIMEAudioType="audio";
	public static final String MIMEImageType="image";
	public static final String MIMETextType="text";
	public static final String MIMEMessageType="message";

	public static final String MIMEMixedSubType="mixed";
	public static final String MIMEOctetStreamSubType="octet-stream";
	public static final String MIMETiffSubType="tiff";
	public static final String MIMEPlainSubType="plain";
	public static final String MIMEHtmlSubType="html";
	public static final String MIMEAlternativeSubType="alternative";
	public static final String MIMERfc822SubType="rfc822";
	public static final String MIMEVoiceMsgSubType="voice-message";
	public static final String MIMEFaxMsgSubType="fax-message";
	public static final String MIMEDirectorySubType="directory";
	public static final String MIME32KADPCMSubType="32KADPCM";
	public static final String MIMEReportSubType="report";
	public static final String MIMEDlvryStatusSubType="delivery-status";
	public static final String MIMERelatedSubType="related";
	public static final String MIMEParallelSubType="parallel";
	public static final String MIMEDigestSubType="digest";
	public static final String MIMEPngSubType="png";
	public static final String MIMEWaveSubType="wav";
	public static final String MIMEMSGSMSubType="msgsm";
	public static final String MIMEBasicSubType="basic";

		/* some non-standard multipart sub-types */
	public static final String MIMEAppleDoubleSubType="appledouble";
	public static final String MIMEMSTNEFSubType="ms-tnef";
	public static final String MIMESMSMsgSubType="sms-message";

		/* some keywords */
	public static final String MIMEBoundaryKeyword="boundary";
	public static final String MIMEFilenameKeyword="filename";
	public static final String MIMECharsetKeyword="charset";
	public static final String MIMENameKeyword="name";
	public static final String MIMEProfileKeyword="profile";
	public static final String MIMEVoiceKeyword="voice";
	public static final String MIMEVversionKeyword="version";
	public static final String MIMETypeKeyword="type";
	public static final String MIMEStartKeyword="start";
	public static final String MIMECodecKeyword="codec";

	public static final String MIMEAttachmentDisp="attachment";
	public static final String MIMEInlineDisp="inline";

		/* some useful parsing functions */

   // Note: mapper must be pre-initialized (i.e. capacity and case-sensitivity)
	public native static int ParseRFC822PropsList (String lpszPropsList, CStr2StrMapper propsMap);
	
	public native static int ValidateRFC822EmailAddr (String addr);
	
	public static final String NonMailUserLocalPart="non-mail-user";
	public static final String NonMailUserMailDomainPart="missing.domain.net";

	public static boolean IsNonMailUserEmailAddress (String addr)
	{
	   if (null == addr)
	      return false;
	   int   iDomainSepPos=addr.indexOf('@');
	   if (iDomainSepPos <= 0)
	      return false;

      if (!NonMailUserLocalPart.equalsIgnoreCase(addr.substring(0, iDomainSepPos-1)))
         return false;
      if (!NonMailUserMailDomainPart.equalsIgnoreCase(addr.substring(iDomainSepPos+1)))
         return false;

      return true;
	}

   public native static String BuildRFC822AddrPair (String szRecipName	/* may be NULL */, String szRecipAddr) throws Util32Errors;

   // position 0 is the name (can be null/empty), position 1 is the address
   public native static String[] DecodeRFC822AddrPair (String szAddrPair) throws Util32Errors;

	// returns a BAD value if unknown string
	public native static byte toRFC822Encoding (String aEnc);

	public native static String toRFC822EncString (byte e);
}